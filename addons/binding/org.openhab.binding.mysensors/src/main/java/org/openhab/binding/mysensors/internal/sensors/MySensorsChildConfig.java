package org.openhab.binding.mysensors.internal.sensors;

import org.openhab.binding.mysensors.internal.Mergeable;
import org.openhab.binding.mysensors.internal.exception.MergeException;

public class MySensorsChildConfig implements Mergeable {

    private boolean requestAck;
    private boolean revertState;
    private boolean smartSleep;
    private int expectUpdateTimeout;

    public MySensorsChildConfig() {
        requestAck = false;
        revertState = false;
        smartSleep = false;
        expectUpdateTimeout = -1;
    }

    public boolean getSmartSleep() {
        return smartSleep;
    }

    public void setSmartSleep(boolean smartSleep) {
        this.smartSleep = smartSleep;
    }

    public int getExpectUpdateTimeout() {
        return expectUpdateTimeout;
    }

    public void setExpectUpdateTimeout(int expectUpdateTimeout) {
        this.expectUpdateTimeout = expectUpdateTimeout;
    }

    public boolean setRequestAck() {
        return requestAck;
    }

    public void setRequestAck(boolean requestAck) {
        this.requestAck = requestAck;
    }

    public boolean setRevertState() {
        return revertState;
    }

    public void setRevertState(boolean revertState) {
        this.revertState = revertState;
    }

    @Override
    public void merge(Object o) throws MergeException {
        if (o == null || !(o instanceof MySensorsChildConfig)) {
            throw new MergeException("Invalid object to merge");
        }

        MySensorsChildConfig childConfig = (MySensorsChildConfig) o;

        requestAck |= childConfig.requestAck;
        revertState |= childConfig.revertState;
        smartSleep |= childConfig.smartSleep;

        if (expectUpdateTimeout <= 0) {
            expectUpdateTimeout = childConfig.expectUpdateTimeout;
        }
    }

    @Override
    public String toString() {
        return "MySensorsChildConfig [requestAck=" + requestAck + ", revertState=" + revertState + ", smartSleep="
                + smartSleep + ", expectUpdateTimeout=" + expectUpdateTimeout + "]";
    }

}
