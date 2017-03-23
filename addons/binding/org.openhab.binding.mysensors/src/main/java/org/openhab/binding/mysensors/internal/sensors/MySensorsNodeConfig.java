package org.openhab.binding.mysensors.internal.sensors;

import org.openhab.binding.mysensors.internal.Mergeable;
import org.openhab.binding.mysensors.internal.exception.MergeException;

public class MySensorsNodeConfig implements Mergeable {

    private boolean requestHeartbeatResponse;
    private int expectUpdateTimeout;

    public MySensorsNodeConfig() {
        requestHeartbeatResponse = false;
        expectUpdateTimeout = -1;
    }

    public boolean getRequestHeartbeatResponse() {
        return requestHeartbeatResponse;
    }

    public void setRequestHeartbeatResponse(boolean requestHeartbeatResponse) {
        this.requestHeartbeatResponse = requestHeartbeatResponse;
    }

    public int getExpectUpdateTimeout() {
        return expectUpdateTimeout;
    }

    public void setExpectUpdateTimeout(int expectUpdateTimeout) {
        this.expectUpdateTimeout = expectUpdateTimeout;
    }

    @Override
    public void merge(Object o) throws MergeException {
        if (o == null || !(o instanceof MySensorsNodeConfig)) {
            throw new MergeException("Invalid object to merge");
        }

        MySensorsNodeConfig nodeConfig = (MySensorsNodeConfig) o;

        requestHeartbeatResponse |= nodeConfig.requestHeartbeatResponse;

        if (expectUpdateTimeout <= 0) {
            expectUpdateTimeout = nodeConfig.expectUpdateTimeout;
        }

    }

    @Override
    public String toString() {
        return "MySensorsNodeConfig [requestHeartbeatResponse=" + requestHeartbeatResponse + ", expectUpdateTimeout="
                + expectUpdateTimeout + "]";
    }

}
