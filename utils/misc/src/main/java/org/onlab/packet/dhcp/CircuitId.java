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

package org.onlab.packet.dhcp;

import com.google.common.collect.Lists;
import org.onlab.packet.VlanId;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Representation of DHCP option 82 Circuit id.
 */
public class CircuitId {
    private static final String SEPARATOR = ":";
    private static final String CIRCUIT_ID_FORMAT = "%s" + SEPARATOR + "%s";
    private String connectPoint;
    private VlanId vlanId;

    /**
     * Creates a circuit id by given information.
     *
     * @param connectPoint the connect point of circuit id
     * @param vlanId the vlan id of circuit id
     */
    public CircuitId(String connectPoint, VlanId vlanId) {
        this.connectPoint = connectPoint;
        this.vlanId = vlanId;
    }

    /**
     * Combines connect point with vlan id with separator ':' as circuit id.
     * e.g. of:0000000000000204/1:100
     *
     * @return serialized circuit id for connect point and vlan ID
     */
    public byte[] serialize() {
        return String
                .format(CIRCUIT_ID_FORMAT, connectPoint, vlanId.toString())
                .getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Deserialize circuit id from byte string.
     *
     * @param circuitId the circuit id byte string
     * @return a Circuit Id
     */
    public static CircuitId deserialize(byte[] circuitId) {
        String cIdString = new String(circuitId, StandardCharsets.US_ASCII);
        List<String> split = Lists.newArrayList(cIdString.split(SEPARATOR));
        checkArgument(split.size() > 1, "Illegal circuit id.");
        // remove last element (vlan id)
        String vlanId = split.remove(split.size() - 1);
        String connectPoint = String.join(SEPARATOR, split);
        return new CircuitId(connectPoint, VlanId.vlanId(vlanId));
    }

    /**
     * Gets the connect point of circuit id.
     *
     * @return the connect point
     */
    public String connectPoint() {
        return connectPoint;
    }

    /**
     * Gets the vlan id of circuit id.
     *
     * @return the vlan id
     */
    public VlanId vlanId() {
        return vlanId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CircuitId)) {
            return false;
        }
        CircuitId that = (CircuitId) obj;
        return Objects.equals(this.connectPoint, that.connectPoint) &&
                Objects.equals(this.vlanId, that.vlanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectPoint, vlanId);
    }
}
