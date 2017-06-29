package org.openhab.binding.mysensors.internal.protocol.message;

/**
 * A MySensors Message consists of 6 parts splitted by semicolon 
 * 
 * @author Tim Oberföll
 *
 */
public enum MySensorsMessagePart {
    NODE        (0),
    CHILD       (1),
    TYPE        (2),
    ACK         (3),
    SUBTYPE     (4),
    PAYLOAD     (5);
    
    private final int id;
    
    private MySensorsMessagePart(int id) {
        this.id = id;
    }
    
    public final int getId() {
        return id;
    }
}
