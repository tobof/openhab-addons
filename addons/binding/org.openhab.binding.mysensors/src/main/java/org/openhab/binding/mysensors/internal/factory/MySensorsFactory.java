package org.openhab.binding.mysensors.internal.factory;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;
import static org.openhab.binding.mysensors.internal.MySensorsUtility.invertMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.mysensors.config.MySensorsSensorConfiguration;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.openhab.binding.mysensors.internal.sensors.type.MySensorsType;

public class MySensorsFactory {
    public static MySensorsNode buildNodeFromThing(Thing t) throws Throwable {
        MySensorsNode ret = null;
        MySensorsSensorConfiguration conf = t.getConfiguration().as(MySensorsSensorConfiguration.class);
        MySensorsChild child = buildChildsFromThing(Integer.parseInt(conf.childId), t);

        ret = new MySensorsNode(Integer.parseInt(conf.nodeId));

        ret.addChild(child);

        return ret;
    }

    public static MySensorsChild buildChildsFromThing(int childId, Thing t) throws Throwable {
        MySensorsChild ret = null;

        Map<Integer, MySensorsVariable> map = buildVariablesFromThing(t, TYPE_MAP, invertMap(CHANNEL_MAP));

        ret = new MySensorsChild(childId, map);

        return ret;
    }

    public static Map<Integer, MySensorsVariable> buildVariablesFromThing(Thing t,
            Map<String, Class<? extends MySensorsType>> typeMap, Map<String, Integer> reverseChannelMap)
            throws Throwable {

        Map<Integer, MySensorsVariable> ret = new HashMap<Integer, MySensorsVariable>();
        List<Channel> channels = t.getChannels();

        for (Channel c : channels) {
            ChannelTypeUID channel = c.getChannelTypeUID();
            Integer variableNum = reverseChannelMap.get(channel);
            Class<? extends MySensorsType> cls = typeMap.get(channel);
            MySensorsVariable var = getVariable(variableNum, cls.newInstance());
            ret.put(variableNum, var);
        }

        return ret;
    }

    private static MySensorsVariable getVariable(int variableNum, MySensorsType type) throws Throwable {
        return new MySensorsVariable(variableNum, type);
    }

    public static void mergeNode(MySensorsNode node) {
        // TODO
    }
}
