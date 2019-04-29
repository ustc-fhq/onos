/*
* Copyright 2017-present Open Networking Laboratory
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.onosproject.openstacknetworking.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.TpPort;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.openstacknetworking.api.InstancePort;
import org.onosproject.openstacknetworking.api.InstancePortEvent;
import org.onosproject.openstacknetworking.api.InstancePortListener;
import org.onosproject.openstacknetworking.api.InstancePortService;
import org.onosproject.openstacknetworking.api.OpenstackFlowRuleService;
import org.onosproject.openstacknetworking.api.OpenstackNetworkEvent;
import org.onosproject.openstacknetworking.api.OpenstackNetworkListener;
import org.onosproject.openstacknetworking.api.OpenstackNetworkService;
import org.onosproject.openstacknetworking.api.OpenstackSecurityGroupEvent;
import org.onosproject.openstacknetworking.api.OpenstackSecurityGroupListener;
import org.onosproject.openstacknetworking.api.OpenstackSecurityGroupService;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.SecurityGroup;
import org.openstack4j.model.network.SecurityGroupRule;
import org.openstack4j.openstack.networking.domain.NeutronSecurityGroupRule;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.openstacknetworking.api.Constants.ACL_TABLE;
import static org.onosproject.openstacknetworking.api.Constants.OPENSTACK_NETWORKING_APP_ID;
import static org.onosproject.openstacknetworking.api.Constants.PRIORITY_ACL_RULE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Populates flow rules to handle OpenStack SecurityGroups.
 */
@Component(immediate = true)
public class OpenstackSecurityGroupHandler {

    private final Logger log = getLogger(getClass());

    private static final boolean USE_SECURITY_GROUP = false;

    @Property(name = "useSecurityGroup", boolValue = USE_SECURITY_GROUP,
            label = "Apply OpenStack security group rule for VM traffic")
    private boolean useSecurityGroup = USE_SECURITY_GROUP;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected InstancePortService instancePortService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackNetworkService osNetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackSecurityGroupService securityGroupService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackFlowRuleService osFlowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService configService;

    private final InstancePortListener instancePortListener = new InternalInstancePortListener();
    private final OpenstackNetworkListener portListener = new InternalOpenstackPortListener();
    private final OpenstackSecurityGroupListener securityGroupListener = new InternalSecurityGroupListener();
    private ApplicationId appId;

    private final ExecutorService eventExecutor = newSingleThreadExecutor(
            groupedThreads(this.getClass().getSimpleName(), "event-handler"));

    private static final String PROTO_ICMP = "ICMP";
    private static final String PROTO_TCP = "TCP";
    private static final String PROTO_UDP = "UDP";
    private static final String ETHTYPE_IPV4 = "IPV4";
    private static final String EGRESS = "EGRESS";
    private static final String INGRESS = "INGRESS";
    private static final IpPrefix IP_PREFIX_ANY = Ip4Prefix.valueOf("0.0.0.0/0");

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(OPENSTACK_NETWORKING_APP_ID);
        instancePortService.addListener(instancePortListener);
        securityGroupService.addListener(securityGroupListener);
        osNetService.addListener(portListener);
        configService.registerProperties(getClass());

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        instancePortService.removeListener(instancePortListener);
        securityGroupService.removeListener(securityGroupListener);
        osNetService.removeListener(portListener);
        configService.unregisterProperties(getClass(), false);
        eventExecutor.shutdown();

        log.info("Stopped");
    }

    @Modified
    protected void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();
        Boolean flag;

        flag = Tools.isPropertyEnabled(properties, "useSecurityGroup");
        if (flag == null) {
            log.info("useSecurityGroup is not configured, " +
                    "using current value of {}", useSecurityGroup);
        } else {
            useSecurityGroup = flag;
            log.info("Configured. useSecurityGroup is {}",
                    useSecurityGroup ? "enabled" : "disabled");
        }

        resetSecurityGroupRules();
    }

    private void setSecurityGroupRules(InstancePort instPort, Port port, boolean install) {
        port.getSecurityGroups().forEach(sgId -> {
            SecurityGroup sg = securityGroupService.securityGroup(sgId);
            if (sg == null) {
                log.error("Security Group Not Found : {}", sgId);
                return;
            }
            sg.getRules().forEach(sgRule -> updateSecurityGroupRule(instPort, port, sgRule, install));
            final String action = install ? "Installed " : "Removed ";
            log.debug(action + "security group rule ID : " + sgId);
        });
    }

    private void updateSecurityGroupRule(InstancePort instPort, Port port, SecurityGroupRule sgRule, boolean install) {

        if (sgRule.getRemoteGroupId() != null && !sgRule.getRemoteGroupId().isEmpty()) {
            getRemoteInstPorts(port.getTenantId(), sgRule.getRemoteGroupId())
                .forEach(rInstPort -> {
                    populateSecurityGroupRule(sgRule, instPort, rInstPort.ipAddress().toIpPrefix(), install);
                    populateSecurityGroupRule(sgRule, rInstPort, instPort.ipAddress().toIpPrefix(), install);

                    SecurityGroupRule rSgRule = new NeutronSecurityGroupRule.SecurityGroupRuleConcreteBuilder()
                            .from(sgRule)
                            .direction(sgRule.getDirection().toUpperCase().equals(EGRESS) ? INGRESS : EGRESS).build();
                    populateSecurityGroupRule(rSgRule, instPort, rInstPort.ipAddress().toIpPrefix(), install);
                    populateSecurityGroupRule(rSgRule, rInstPort, instPort.ipAddress().toIpPrefix(), install);
                });
        } else {
            populateSecurityGroupRule(sgRule, instPort, sgRule.getRemoteIpPrefix() == null ? IP_PREFIX_ANY :
                    IpPrefix.valueOf(sgRule.getRemoteIpPrefix()), install);
        }
    }

    private void populateSecurityGroupRule(SecurityGroupRule sgRule, InstancePort instPort,
                                           IpPrefix remoteIp, boolean install) {
        Set<TrafficSelector> selectors = buildSelectors(sgRule,
                Ip4Address.valueOf(instPort.ipAddress().toInetAddress()), remoteIp);
        if (selectors == null || selectors.isEmpty()) {
            return;
        }

        selectors.forEach(selector -> {
            osFlowRuleService.setRule(appId,
                    instPort.deviceId(),
                    selector,
                    DefaultTrafficTreatment.builder().build(),
                    PRIORITY_ACL_RULE,
                    ACL_TABLE,
                    install);
        });
    }

    /**
     * Returns a set of host IP addresses engaged with supplied security group ID.
     * It only searches a VM in the same tenant boundary.
     *
     * @param tenantId tenant id
     * @param sgId security group id
     * @return set of ip addresses
     */
    private Set<InstancePort> getRemoteInstPorts(String tenantId, String sgId) {
        Set<InstancePort> remoteInstPorts;

        remoteInstPorts = osNetService.ports().stream()
                .filter(port -> port.getTenantId().equals(tenantId))
                .filter(port -> port.getSecurityGroups().contains(sgId))
                .map(port -> instancePortService.instancePort(port.getId()))
                .filter(instPort -> instPort != null && instPort.ipAddress() != null)
                .collect(Collectors.toSet());

        return Collections.unmodifiableSet(remoteInstPorts);
    }

    private Set<TrafficSelector> buildSelectors(SecurityGroupRule sgRule,
                                                Ip4Address vmIp,
                                                IpPrefix remoteIp) {
        if (remoteIp != null && remoteIp.equals(IpPrefix.valueOf(vmIp, 32))) {
            // do nothing if the remote IP is my IP
            return null;
        }

        Set<TrafficSelector> selectorSet = Sets.newHashSet();

        TrafficSelector.Builder sBuilder = DefaultTrafficSelector.builder();
        buildMatchs(sBuilder, sgRule, vmIp, remoteIp);

        if (sgRule.getPortRangeMax() != null && sgRule.getPortRangeMin() != null &&
                sgRule.getPortRangeMin() < sgRule.getPortRangeMax()) {
            Map<TpPort, TpPort> portRangeMatchMap = buildPortRangeMatches(sgRule.getPortRangeMin(),
                    sgRule.getPortRangeMax());
            portRangeMatchMap.entrySet().forEach(entry -> {

                if (sgRule.getProtocol().toUpperCase().equals(PROTO_TCP)) {
                    if (sgRule.getDirection().toUpperCase().equals(EGRESS)) {
                        sBuilder.matchTcpSrcMasked(entry.getKey(), entry.getValue());
                    } else {
                        sBuilder.matchTcpDstMasked(entry.getKey(), entry.getValue());
                    }
                } else if (sgRule.getProtocol().toUpperCase().equals(PROTO_UDP)) {
                    if (sgRule.getDirection().toUpperCase().equals(EGRESS)) {
                        sBuilder.matchUdpSrcMasked(entry.getKey(), entry.getValue());
                    } else {
                        sBuilder.matchUdpDstMasked(entry.getKey(), entry.getValue());
                    }
                }

                selectorSet.add(sBuilder.build());
                }
            );
        } else {
            selectorSet.add(sBuilder.build());
        }

        return selectorSet;
    }

    private void buildMatchs(TrafficSelector.Builder sBuilder, SecurityGroupRule sgRule,
                             Ip4Address vmIp, IpPrefix remoteIp) {
        buildMatchEthType(sBuilder, sgRule.getEtherType());
        buildMatchDirection(sBuilder, sgRule.getDirection(), vmIp);
        buildMatchProto(sBuilder, sgRule.getProtocol());
        buildMatchPort(sBuilder, sgRule.getProtocol(), sgRule.getDirection(),
                sgRule.getPortRangeMin() == null ? 0 : sgRule.getPortRangeMin(),
                sgRule.getPortRangeMax() == null ? 0 : sgRule.getPortRangeMax());
        buildMatchRemoteIp(sBuilder, remoteIp, sgRule.getDirection());
        if (sgRule.getRemoteGroupId() != null && sgRule.getRemoteGroupId().isEmpty()) {
            buildMatchRemoteIp(sBuilder, remoteIp, sgRule.getDirection());
        }
    }

    private void buildMatchDirection(TrafficSelector.Builder sBuilder,
                                     String direction,
                                     Ip4Address vmIp) {
        if (direction.toUpperCase().equals(EGRESS)) {
            sBuilder.matchIPSrc(IpPrefix.valueOf(vmIp, 32));
        } else {
            sBuilder.matchIPDst(IpPrefix.valueOf(vmIp, 32));
        }
    }

    private void buildMatchEthType(TrafficSelector.Builder sBuilder, String etherType) {
        // Either IpSrc or IpDst (or both) is set by default, and we need to set EthType as IPv4.
        sBuilder.matchEthType(Ethernet.TYPE_IPV4);
        if (etherType != null && !Objects.equals(etherType, "null") &&
                !etherType.toUpperCase().equals(ETHTYPE_IPV4)) {
            log.debug("EthType {} is not supported yet in Security Group", etherType);
        }
    }

    private void buildMatchRemoteIp(TrafficSelector.Builder sBuilder, IpPrefix remoteIpPrefix, String direction) {
        if (remoteIpPrefix != null && !remoteIpPrefix.getIp4Prefix().equals(IP_PREFIX_ANY)) {
            if (direction.toUpperCase().equals(EGRESS)) {
                sBuilder.matchIPDst(remoteIpPrefix);
            } else {
                sBuilder.matchIPSrc(remoteIpPrefix);
            }
        }
    }

    private void buildMatchProto(TrafficSelector.Builder sBuilder, String protocol) {
        if (protocol != null) {
            switch (protocol.toUpperCase()) {
                case PROTO_ICMP:
                    sBuilder.matchIPProtocol(IPv4.PROTOCOL_ICMP);
                    break;
                case PROTO_TCP:
                    sBuilder.matchIPProtocol(IPv4.PROTOCOL_TCP);
                    break;
                case PROTO_UDP:
                    sBuilder.matchIPProtocol(IPv4.PROTOCOL_UDP);
                    break;
                default:
            }
        }
    }

    private void buildMatchPort(TrafficSelector.Builder sBuilder, String protocol, String direction,
                                int portMin, int portMax) {
        if (portMin > 0 && portMax > 0 && portMin == portMax) {
            if (protocol.toUpperCase().equals(PROTO_TCP)) {
                if (direction.toUpperCase().equals(EGRESS)) {
                    sBuilder.matchTcpSrc(TpPort.tpPort(portMax));
                } else {
                    sBuilder.matchTcpDst(TpPort.tpPort(portMax));
                }
            } else if (protocol.toUpperCase().equals(PROTO_UDP)) {
                if (direction.toUpperCase().equals(EGRESS)) {
                    sBuilder.matchUdpSrc(TpPort.tpPort(portMax));
                } else {
                    sBuilder.matchUdpDst(TpPort.tpPort(portMax));
                }
            }
        }
    }

    private void resetSecurityGroupRules() {

        if (useSecurityGroup) {
            securityGroupService.securityGroups().forEach(securityGroup ->
                    securityGroup.getRules().forEach(this::securityGroupRuleAdded));
        } else {
            securityGroupService.securityGroups().forEach(securityGroup ->
                    securityGroup.getRules().forEach(this::securityGroupRuleRemoved));
        }

        log.info("Reset security group info " + (useSecurityGroup ? " with " : " without") + " Security Group");
    }

    private void securityGroupRuleAdded(SecurityGroupRule sgRule) {
        osNetService.ports().stream()
                .filter(port -> port.getSecurityGroups().contains(sgRule.getSecurityGroupId()))
                .forEach(port -> {
                    updateSecurityGroupRule(
                            instancePortService.instancePort(port.getId()),
                            port, sgRule, true);
                    log.debug("Applied security group rule {} to port {}",
                            sgRule.getId(), port.getId());
                });
    }

    private void securityGroupRuleRemoved(SecurityGroupRule sgRule) {
        osNetService.ports().stream()
                .filter(port -> port.getSecurityGroups().contains(sgRule.getSecurityGroupId()))
                .forEach(port -> {
                    updateSecurityGroupRule(
                            instancePortService.instancePort(port.getId()),
                            port, sgRule, false);
                    log.debug("Removed security group rule {} from port {}",
                            sgRule.getId(), port.getId());
                });
    }

    private int binLower(String binStr, int bits) {
        String outBin = binStr.substring(0, 16 - bits);
        for (int i = 0; i < bits; i++) {
            outBin += "0";
        }

        return Integer.parseInt(outBin, 2);
    }

    private int binHigher(String binStr, int bits) {
        String outBin = binStr.substring(0, 16 - bits);
        for (int i = 0; i < bits; i++) {
            outBin += "1";
        }

        return Integer.parseInt(outBin, 2);
    }

    private int testMasks(String binStr, int start, int end) {
        int mask = 0;
        for (; mask <= 16; mask++) {
            int maskStart = binLower(binStr, mask);
            int maskEnd = binHigher(binStr, mask);
            if (maskStart < start || maskEnd > end) {
                return mask - 1;
            }
        }

        return mask;
    }

    private String getMask(int bits) {
        switch (bits) {
            case 0:  return "ffff";
            case 1:  return "fffe";
            case 2:  return "fffc";
            case 3:  return "fff8";
            case 4:  return "fff0";
            case 5:  return "ffe0";
            case 6:  return "ffc0";
            case 7:  return "ff80";
            case 8:  return "ff00";
            case 9:  return "fe00";
            case 10: return "fc00";
            case 11: return "f800";
            case 12: return "f000";
            case 13: return "e000";
            case 14: return "c000";
            case 15: return "8000";
            case 16: return "0000";
            default: return null;
        }
    }

    private Map<TpPort, TpPort> buildPortRangeMatches(int portMin, int portMax) {

        boolean processing = true;
        int start = portMin;
        Map<TpPort, TpPort> portMaskMap = Maps.newHashMap();
        while (processing) {
            String minStr = Integer.toBinaryString(start);
            String binStrMinPadded = "0000000000000000".substring(minStr.length()) + minStr;

            int mask = testMasks(binStrMinPadded, start, portMax);
            int maskStart = binLower(binStrMinPadded, mask);
            int maskEnd = binHigher(binStrMinPadded, mask);

            log.debug("start : {} port/mask = {} / {} ", start, getMask(mask), maskStart);
            portMaskMap.put(TpPort.tpPort(maskStart), TpPort.tpPort(Integer.parseInt(getMask(mask), 16)));

            start = maskEnd + 1;
            if (start > portMax) {
                processing = false;
            }
        }

        return portMaskMap;
    }

    private class InternalInstancePortListener implements InstancePortListener {

        @Override
        public boolean isRelevant(InstancePortEvent event) {
            InstancePort instPort = event.subject();
            if (!useSecurityGroup) {
                return false;
            }
            return mastershipService.isLocalMaster(instPort.deviceId());
        }

        @Override
        public void event(InstancePortEvent event) {
            InstancePort instPort = event.subject();
            switch (event.type()) {
                case OPENSTACK_INSTANCE_PORT_UPDATED:
                case OPENSTACK_INSTANCE_PORT_DETECTED:
                    log.debug("Instance port detected MAC:{} IP:{}",
                            instPort.macAddress(),
                            instPort.ipAddress());
                    eventExecutor.execute(() -> {
                        setSecurityGroupRules(instPort,
                                osNetService.port(event.subject().portId()),
                                true);
                    });
                    break;
                case OPENSTACK_INSTANCE_PORT_VANISHED:
                    log.debug("Instance port vanished MAC:{} IP:{}",
                            instPort.macAddress(),
                            instPort.ipAddress());
                    eventExecutor.execute(() -> {
                        setSecurityGroupRules(instPort,
                                osNetService.port(event.subject().portId()),
                                false);
                    });
                    break;
                default:
                    break;
            }
        }
    }

    private class InternalOpenstackPortListener implements OpenstackNetworkListener {

        @Override
        public boolean isRelevant(OpenstackNetworkEvent event) {
            if (event.port() == null || !Strings.isNullOrEmpty(event.port().getId())) {
                return false;
            }
            if (event.securityGroupId() == null ||
                    securityGroupService.securityGroup(event.securityGroupId()) == null) {
                return false;
            }
            if (instancePortService.instancePort(event.port().getId()) == null) {
                return false;
            }
            if (!useSecurityGroup) {
                return false;
            }
            return true;
        }

        @Override
        public void event(OpenstackNetworkEvent event) {
            Port osPort = event.port();
            InstancePort instPort = instancePortService.instancePort(osPort.getId());
            SecurityGroup osSg = securityGroupService.securityGroup(event.securityGroupId());

            switch (event.type()) {
                case OPENSTACK_PORT_SECURITY_GROUP_ADDED:
                    eventExecutor.execute(() -> {
                        osSg.getRules().forEach(sgRule -> {
                            updateSecurityGroupRule(instPort, osPort, sgRule, true);
                        });
                        log.info("Added security group {} to port {}",
                                event.securityGroupId(), event.port().getId());
                    });
                    break;
                case OPENSTACK_PORT_SECURITY_GROUP_REMOVED:
                    eventExecutor.execute(() -> {
                        osSg.getRules().forEach(sgRule -> {
                            updateSecurityGroupRule(instPort, osPort, sgRule, false);
                        });
                        log.info("Removed security group {} from port {}",
                                event.securityGroupId(), event.port().getId());
                    });
                    break;
                default:
                    // do nothing for the other events
                    break;
            }
        }
    }

    private class InternalSecurityGroupListener implements OpenstackSecurityGroupListener {

        @Override
        public boolean isRelevant(OpenstackSecurityGroupEvent event) {
            if (!useSecurityGroup) {
                return false;
            }
            return true;
        }

        @Override
        public void event(OpenstackSecurityGroupEvent event) {
            switch (event.type()) {
                case OPENSTACK_SECURITY_GROUP_RULE_CREATED:
                    SecurityGroupRule securityGroupRuleToAdd = event.securityGroupRule();
                    eventExecutor.execute(() -> {
                        securityGroupRuleAdded(securityGroupRuleToAdd);
                        log.info("Applied new security group rule {} to ports",
                                securityGroupRuleToAdd.getId());
                    });
                    break;

                case OPENSTACK_SECURITY_GROUP_RULE_REMOVED:
                    SecurityGroupRule securityGroupRuleToRemove = event.securityGroupRule();
                    eventExecutor.execute(() -> {
                        securityGroupRuleRemoved(securityGroupRuleToRemove);
                        log.info("Removed security group rule {} from ports",
                                securityGroupRuleToRemove.getId());
                    });
                    break;
                case OPENSTACK_SECURITY_GROUP_CREATED:
                case OPENSTACK_SECURITY_GROUP_REMOVED:
                default:
                    // do nothing
                    break;
            }
        }
    }
}
