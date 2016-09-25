/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.event;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

/**
 * @author Tim Oberföll
 *
 *         If a new message from the gateway/bridge is received
 *         a MySensorsStatusUpdateEvent is generated containing the MySensors message
 */
public class MySensorsStatusUpdateEvent {
    private Object data;

    private MySensorsEventType eventType;

    public MySensorsStatusUpdateEvent(Object data) {
        this.data = data;
    }

    public void setEventType(MySensorsEventType event) {
        this.eventType = event;
    }

    public MySensorsEventType getEventType() {
        return eventType;
    }

    public Object getData() {
        return data;
    }

    public void setData(MySensorsMessage data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MySensorsStatusUpdateEvent other = (MySensorsStatusUpdateEvent) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        if (eventType != other.eventType) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsStatusUpdateEvent [data=" + data + ", event=" + eventType + "]";
    }
}
