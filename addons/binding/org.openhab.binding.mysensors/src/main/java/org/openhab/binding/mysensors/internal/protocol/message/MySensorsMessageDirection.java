package org.openhab.binding.mysensors.internal.protocol.message;

/**
 * Differentiates between the directions of a message.
 * Incoming == from the gateway to the binding
 * Outgoing == from the binding to the gateway
 * 
 * @author Tim Oberföll
 *
 */
public enum MySensorsMessageDirection {
    INCOMING (0),
    OUTGOING (1);
    
    private final int id;
    
    private MySensorsMessageDirection(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
}
