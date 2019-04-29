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

package org.onosproject.net.pi.model;

import com.google.common.annotations.Beta;
import org.onosproject.net.driver.HandlerBehaviour;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.pi.runtime.PiHeaderFieldId;
import org.onosproject.net.pi.runtime.PiPacketOperation;
import org.onosproject.net.pi.runtime.PiTableAction;
import org.onosproject.net.pi.runtime.PiTableId;

import java.util.Collection;
import java.util.Optional;

/**
 * An interpreter of a protocol-independent pipeline model.
 */
@Beta
public interface PiPipelineInterpreter extends HandlerBehaviour {

    /**
     * Returns the protocol-independent header field identifier that is equivalent to the given
     * criterion type, if present. If not present, it means that the given criterion type is not
     * supported by this interpreter.
     *
     * @param type criterion type
     * @return optional header field identifier
     */
    Optional<PiHeaderFieldId> mapCriterionType(Criterion.Type type);

    /**
     * Returns the criterion type that is equivalent to the given protocol-independent header field
     * identifier, if present. If not present, it means that the given field identifier is not
     * supported by this interpreter.
     *
     * @param headerFieldId header field identifier
     * @return optional criterion type
     */
    Optional<Criterion.Type> mapPiHeaderFieldId(PiHeaderFieldId headerFieldId);

    /**
     * Returns a protocol-independent table id equivalent to the given numeric table id (as in
     * {@link FlowRule#tableId()}). If not present, it means that the given numeric table id cannot
     * be mapped to any table of the pipeline model.
     *
     * @param flowRuleTableId a numeric table id
     * @return a protocol-independent table id
     */
    Optional<PiTableId> mapFlowRuleTableId(int flowRuleTableId);

    /**
     * Returns a table action of a protocol-independent pipeline that is functionally equivalent to
     * the given ONOS traffic treatment for the given pipeline configuration.
     *
     * @param treatment a ONOS traffic treatment
     * @param pipeconf  a pipeline configuration
     * @return a table action object
     * @throws PiInterpreterException if the treatment cannot be mapped to any table action
     */
    PiTableAction mapTreatment(TrafficTreatment treatment, PiPipeconf pipeconf)
            throws PiInterpreterException;

    /**
     * Returns a collection of packet operations equivalent to the given OutboundPacket, for the given
     * pipeline configuration.
     *
     * @param packet   a ONOS outbound packet
     * @param pipeconf a pipeline configuration
     * @return a collection of packet operations
     * @throws PiInterpreterException if the packet treatments cannot be mapped to any metadata
     */
    Collection<PiPacketOperation> mapOutboundPacket(OutboundPacket packet, PiPipeconf pipeconf)
            throws PiInterpreterException;

    /**
     * Signals that an error was encountered while executing the interpreter.
     */
    @Beta
    class PiInterpreterException extends Exception {
        public PiInterpreterException(String message) {
            super(message);
        }
    }
}