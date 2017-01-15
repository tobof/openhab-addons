package org.openhab.binding.mysensors.factory;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.mysensors.config.MySensorsSensorConfiguration;
import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;

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
            String channelID = c.getUID().getId();
            Pair<Integer> variableNum = INVERSE_CHANNEL_MAP.get(channelID);

            if (variableNum == null) {
                if (!channelID.equals(CHANNEL_LAST_UPDATE) && !channelID.equals(CHANNEL_MYSENSORS_MESSAGE)) {
                    throw new NullPointerException(
                            "Variable for channel " + channelID + " not defined in CHANNEL_MAP/TYPE_MAP");
                } else {
                    // CHANNEL_LAST_UPDATE/CHANNEL_MYSENSORS_MESSAGE are NOT standard channel. Their correspondent
                    // channel is not part of MySensors standard protocol. Just continue iteration of other channels

                    continue;
                }
            }

            MySensorsVariable var = new MySensorsVariable(variableNum);

            if (variableNum == null || var == null) {
                throw new IllegalStateException("Command/Type building error");
            }

            ret.put(variableNum, var);
        }

        return ret;
    }
}
