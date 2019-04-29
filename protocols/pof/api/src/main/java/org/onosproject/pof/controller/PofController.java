/*
 * Copyright 2015-present Open Networking Laboratory
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
package org.onosproject.pof.controller;


import org.onosproject.floodlightpof.protocol.OFMessage;

/**
 * Abstraction of a POF controller. Serves as a one stop
 * shop for obtaining POF devices and (un)register listeners
 * on POF events
 */
public interface PofController {

    /**
     * Returns all switches known to this OF controller.
     * @return Iterable of dpid elements
     */
    Iterable<PofSwitch> getSwitches();

    /**
     * Returns all master switches known to this OF controller.
     * @return Iterable of dpid elements
     */
    Iterable<PofSwitch> getMasterSwitches();

    /**
     * Returns all equal switches known to this OF controller.
     * @return Iterable of dpid elements
     */
    Iterable<PofSwitch> getEqualSwitches();


    /**
     * Returns the actual switch for the given Dpid.
     * @param dpid the switch to fetch
     * @return the interface to this switch
     */
    PofSwitch getSwitch(Dpid dpid);

    /**
     * Returns the actual master switch for the given Dpid, if one exists.
     * @param dpid the switch to fetch
     * @return the interface to this switch
     */
    PofSwitch getMasterSwitch(Dpid dpid);

    /**
     * Returns the actual equal switch for the given Dpid, if one exists.
     * @param dpid the switch to fetch
     * @return the interface to this switch
     */
    PofSwitch getEqualSwitch(Dpid dpid);

    /**
     * If this set to be true, all incoming events are monitored.
     * Other wise, only stats related incoming events are monitored
     * @param monitor monitoring flag
     */
    void monitorAllEvents(boolean monitor);

    /**
     * Register a listener for meta events that occur to OF
     * devices.
     * @param listener the listener to notify
     */
    void addListener(PofSwitchListener listener);

    /**
     * Unregister a listener.
     *
     * @param listener the listener to unregister
     */
    void removeListener(PofSwitchListener listener);

    /**
     * Register a listener for packet events.
     * @param priority the importance of this listener, lower values are more important
     * @param listener the listener to notify
     */
    void addPacketListener(int priority, PacketListener listener);

    /**
     * Unregister a listener.
     *
     * @param listener the listener to unregister
     */
    void removePacketListener(PacketListener listener);

    /**
     * Register a listener for OF msg events.
     *
     * @param listener the listener to notify
     */
    void addEventListener(PofEventListener listener);

    /**
     * Unregister a listener.
     *
     * @param listener the listener to unregister
     */
    void removeEventListener(PofEventListener listener);

    /**
     * Send a message to a particular switch.
     * @param dpid the switch to send to.
     * @param msg the message to send
     */
    void write(Dpid dpid, OFMessage msg);

    /**
     * Process a message and notify the appropriate listeners.
     *
     * @param dpid the dpid the message arrived on
     * @param msg the message to process.
     */
    void processPacket(Dpid dpid, OFMessage msg);

    /**
     * Sets the role for a given switch.
     * @param role the desired role
     * @param dpid the switch to set the role for.
     */
    void setRole(Dpid dpid, RoleState role);
}
