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
import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.openhab.binding.mysensors.internal.sensors.type.MySensorsType;

public class MySensorsSensorsFactory {
    public static MySensorsNode buildNodeFromThing(Thing t) throws Throwable {
        MySensorsNode ret = null;
        MySensorsSensorConfiguration conf = t.getConfiguration().as(MySensorsSensorConfiguration.class);
        MySensorsChild child = null;

        ret = new MySensorsNode(Integer.parseInt(conf.nodeId));

        child = buildChildsFromThing(Integer.parseInt(conf.childId), t);
        if (child == null) {
            throw new IllegalStateException("Null child built");
        }

        ret.addChild(child);

        return ret;
    }

    public static MySensorsChild buildChildsFromThing(int childId, Thing t) throws Throwable {
        MySensorsChild ret = null;

        Map<Pair<Integer>, MySensorsVariable> map = buildVariablesFromThing(t);

        ret = new MySensorsChild(childId, map);

        return ret;
    }

    public static Map<Pair<Integer>, MySensorsVariable> buildVariablesFromThing(Thing t) throws Throwable {

        Map<Pair<Integer>, MySensorsVariable> ret = new HashMap<Pair<Integer>, MySensorsVariable>();
        List<Channel> channels = t.getChannels();

        for (Channel c : channels) {
            ChannelTypeUID channel = c.getChannelTypeUID();
            Pair<Integer> variableNum = invertMap(CHANNEL_MAP).get(channel);
            Class<? extends MySensorsType> cls = TYPE_MAP.get(channel);
            MySensorsVariable var = getVariable(variableNum, cls.newInstance());

            if (variableNum == null || var == null) {
                throw new IllegalStateException("Variable number and/or type building error");
            }

            ret.put(variableNum, var);
        }

        return ret;
    }

    private static MySensorsVariable getVariable(Pair<Integer> varTypeAndNumber, MySensorsType type) throws Throwable {
        return new MySensorsVariable(varTypeAndNumber, type);
    }
}
