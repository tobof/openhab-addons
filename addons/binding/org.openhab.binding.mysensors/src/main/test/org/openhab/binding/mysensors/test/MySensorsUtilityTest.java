package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.MySensorsUtility;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;

public class MySensorsUtilityTest {

    @Test
    public void testMergeMap() {
        Map<Integer, MySensorsNode> m1 = new HashMap<>();
        Map<Integer, MySensorsNode> m2 = new HashMap<>();

        m1.put(1, new MySensorsNode(1));
        m1.put(2, new MySensorsNode(2));

        m2.put(4, new MySensorsNode(4));
        m2.put(6, new MySensorsNode(6));
        m2.put(7, new MySensorsNode(7));

        MySensorsUtility.mergeMap(m1, m2);

        assertEquals(5, m1.size());

        assertEquals(1, m1.get(1).getNodeId());
        assertEquals(2, m1.get(2).getNodeId());
        assertEquals(4, m1.get(4).getNodeId());
        assertEquals(6, m1.get(6).getNodeId());
        assertEquals(7, m1.get(7).getNodeId());

    }

    @Test
    public void testtt() {

        final Object lock = new Object();

        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (lock) {
                    try {
                        Thread.sleep(1000000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        });

        t1.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Thread t2 = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println(lock.toString());

            }
        });

        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
