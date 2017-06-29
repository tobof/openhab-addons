package org.openhab.binding.mysensors.internal.protocol.message;

import java.util.HashMap;
import java.util.Map;

/**
 * Every message contains a field with which the sender is able to indicate that it requests an
 * Acknowledgement for the message. 
 * 
 * @author Tim Oberföll
 *
 */
public enum MySensorsMessageAck {
    TRUE    (1),
    FALSE   (0);
    
    private final int id;
    
    private MySensorsMessageAck(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
    
    private static final Map<Integer, MySensorsMessageAck> byId = new HashMap<Integer, MySensorsMessageAck>();
    static {
        for (MySensorsMessageAck e : MySensorsMessageAck.values()) {
            if (byId.put(e.getId(), e) != null) {
                throw new IllegalArgumentException("duplicate id: " + e.getId());
            }
        }
    }

    public static MySensorsMessageAck getById(int id) {
        return byId.get(id);
    }

}
