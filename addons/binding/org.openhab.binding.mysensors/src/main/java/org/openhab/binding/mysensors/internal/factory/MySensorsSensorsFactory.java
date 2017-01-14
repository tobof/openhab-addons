package org.openhab.binding.mysensors.internal.factory;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.mysensors.config.MySensorsSensorConfiguration;
import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChannel;
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

        Map<Pair<Integer>, MySensorsChannel> map = buildVariablesFromThing(t);

        ret = new MySensorsChild(childId, map);

        return ret;
    }

    public static Map<Pair<Integer>, MySensorsChannel> buildVariablesFromThing(Thing t) throws Throwable {

        Map<Pair<Integer>, MySensorsChannel> ret = new HashMap<Pair<Integer>, MySensorsChannel>();
        List<Channel> channels = t.getChannels();

        for (Channel c : channels) {
            String channelID = c.getUID().getId();
            Pair<Integer> variableNum = INVERSE_CHANNEL_MAP.get(channelID);
            Class<? extends MySensorsType> cls = TYPE_MAP.get(channelID);

            if (variableNum == null || cls == null) {
                if (!channelID.equals(CHANNEL_LAST_UPDATE) && !channelID.equals(CHANNEL_MYSENSORS_MESSAGE)) {
                    throw new NullPointerException(
                            "Variable for channel " + channelID + " not defined in CHANNEL_MAP/TYPE_MAP");
                } else {
                    // CHANNEL_LAST_UPDATE/CHANNEL_MYSENSORS_MESSAGE are NOT standard channel. Their correspondent
                    // channel is not part of MySensors standard protocol. Just continue iteration of other channels

                    continue;
                }
            }

            MySensorsChannel var = getVariable(variableNum, cls.newInstance());

            if (variableNum == null || var == null) {
                throw new IllegalStateException("Variable number and/or type building error");
            }

            ret.put(variableNum, var);
        }

        return ret;
    }

    private static MySensorsChannel getVariable(Pair<Integer> varTypeAndNumber, MySensorsType type) throws Throwable {
        return new MySensorsChannel(varTypeAndNumber, type);
    }
}
