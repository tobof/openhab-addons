/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.service;

import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.smarthome.model.script.actions.Ping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkService} handles the connection to the Device
 *
 * @author Marc Mettke
 */
public class NetworkService {

    private static Logger logger = LoggerFactory.getLogger(NetworkService.class);

    private ScheduledFuture<?> refreshJob;

    private String hostname;
    private int port;
    private int retry;
    private long refreshInterval;
    private int timeout;
    private boolean useSystemPing;

    public NetworkService() {
        this("", 0, 1, 60000, 5000, false);
    }

    public NetworkService(String hostname, int port, int retry, long refreshInterval, int timeout,
            boolean useSystemPing) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.retry = retry;
        this.refreshInterval = refreshInterval;
        this.timeout = timeout;
        this.useSystemPing = useSystemPing;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public int getRetry() {
        return retry;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isUseSystemPing() {
        return useSystemPing;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setUseSystemPing(boolean useSystemPing) {
        this.useSystemPing = useSystemPing;
    }

    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService,
            final StateUpdate stateUpdate) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    stateUpdate.newState(updateDeviceState());
                } catch (InvalidConfigurationException e) {
                    stateUpdate.invalidConfig();
                }
            }
        };

        refreshJob = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, refreshInterval, TimeUnit.MILLISECONDS);
    }

    public void stopAutomaticRefresh() {
        refreshJob.cancel(true);
    }

    /**
     * Updates one device to a new status
     */
    public double updateDeviceState() throws InvalidConfigurationException {
        int currentTry = 0;
        double result;

        do {
            result = updateDeviceState(getHostname(), getPort(), getTimeout(), isUseSystemPing());
            currentTry++;
        } while (result < 0 && currentTry < this.retry);

        return result;
    }

    /**
     * Try's to reach the Device by Ping
     */
    private static double updateDeviceState(String hostname, int port, int timeout, boolean useSystemPing)
            throws InvalidConfigurationException {
        boolean success = false;
        double pingTime = -1;

        try {
            if (!useSystemPing) {
                pingTime = System.nanoTime();
                success = Ping.checkVitality(hostname, port, timeout);
                pingTime = System.nanoTime() - pingTime;
            } else {
                Process proc;
                if (SystemUtils.IS_OS_UNIX) {
                    pingTime = System.nanoTime();
                    proc = new ProcessBuilder("ping", "-t", String.valueOf(timeout / 1000), "-c", "1", hostname)
                            .start();
                } else if (SystemUtils.IS_OS_WINDOWS) {
                    pingTime = System.nanoTime();
                    proc = new ProcessBuilder("ping", "-w", String.valueOf(timeout), "-n", "1", hostname).start();
                } else {
                    logger.error("The System Ping is not supported on this Operating System");
                    throw new InvalidConfigurationException("System Ping not supported");
                }

                int exitValue = proc.waitFor();
                pingTime = System.nanoTime() - pingTime;
                success = exitValue == 0;
                if (!success) {
                    logger.debug("Ping stopped with Error Number: " + exitValue + " on Command :" + "ping"
                            + (SystemUtils.IS_OS_UNIX ? " -t " : " -w ")
                            + (SystemUtils.IS_OS_UNIX ? String.valueOf(timeout / 1000) : String.valueOf(timeout))
                            + (SystemUtils.IS_OS_UNIX ? " -c" : " -n") + " 1 " + hostname);
                }
            }

            logger.debug("established connection [host '{}' port '{}' timeout '{}']",
                    new Object[] { hostname, port, timeout });
        } catch (SocketTimeoutException se) {
            logger.debug("timed out while connecting to host '{}' port '{}' timeout '{}'",
                    new Object[] { hostname, port, timeout });
        } catch (IOException ioe) {
            logger.debug("couldn't establish network connection [host '{}' port '{}' timeout '{}']",
                    new Object[] { hostname, port, timeout });
        } catch (InterruptedException e) {
            logger.debug("ping program was interrupted");
        }

        return success ? pingTime / 1000000.0f : -1;

    }

    /**
     * Handles the whole Discovery
     */
    public static void discoverNetwork(DiscoveryCallback discoveryCallback,
            ScheduledExecutorService scheduledExecutorService) {
        TreeSet<String> interfaceIPs;
        LinkedHashSet<String> networkIPs;

        logger.debug("Starting Device Discovery");
        interfaceIPs = getInterfaceIPs();
        networkIPs = getNetworkIPs(interfaceIPs);
        startDiscovery(networkIPs, discoveryCallback, scheduledExecutorService);
    }

    /**
     * Gets every IPv4 Address on each Interface except the loopback
     * The Address format is ip/subnet
     *
     * @return The collected IPv4 Addresses
     */
    private static TreeSet<String> getInterfaceIPs() {
        TreeSet<String> interfaceIPs = new TreeSet<String>();

        try {
            // For each interface ...
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface networkInterface = en.nextElement();
                if (!networkInterface.isLoopback()) {

                    // .. and for each address ...
                    for (Iterator<InterfaceAddress> it = networkInterface.getInterfaceAddresses().iterator(); it
                            .hasNext();) {

                        // ... get IP and Subnet
                        InterfaceAddress interfaceAddress = it.next();
                        interfaceIPs.add(interfaceAddress.getAddress().getHostAddress() + "/"
                                + interfaceAddress.getNetworkPrefixLength());
                    }
                }
            }
        } catch (SocketException e) {
        }

        return interfaceIPs;
    }

    /**
     * Takes the interfaceIPs and fetches every IP which can be assigned on their network
     *
     * @param networkIPs The IPs which are assigned to the Network Interfaces
     * @return Every single IP which can be assigned on the Networks the computer is connected to
     */
    private static LinkedHashSet<String> getNetworkIPs(TreeSet<String> interfaceIPs) {
        LinkedHashSet<String> networkIPs = new LinkedHashSet<String>();

        for (Iterator<String> it = interfaceIPs.iterator(); it.hasNext();) {
            try {
                // gets every ip which can be assigned on the given network
                SubnetUtils utils = new SubnetUtils(it.next());
                String[] addresses = utils.getInfo().getAllAddresses();
                for (int i = 0; i < addresses.length; i++) {
                    networkIPs.add(addresses[i]);
                }

            } catch (Exception ex) {
            }
        }

        return networkIPs;
    }

    /**
     * Starts the DiscoveryThread for each IP on the Networks
     *
     * @param allNetworkIPs
     */
    private static void startDiscovery(final LinkedHashSet<String> networkIPs,
            final DiscoveryCallback discoveryCallback, ScheduledExecutorService scheduledExecutorService) {
        final int PING_TIMEOUT_IN_MS = 500;
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);

        for (Iterator<String> it = networkIPs.iterator(); it.hasNext();) {
            final String ip = it.next();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (ip != null) {
                        try {
                            if (Ping.checkVitality(ip, 0, PING_TIMEOUT_IN_MS)) {
                                discoveryCallback.newDevice(ip);
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            });
        }
        try {
            executorService.awaitTermination(PING_TIMEOUT_IN_MS * networkIPs.size(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        executorService.shutdown();
    }

    @Override
    public String toString() {
        return this.hostname + ";" + this.port + ";" + this.retry + ";" + this.refreshInterval + ";" + this.timeout
                + ";" + this.useSystemPing;
    }
}
