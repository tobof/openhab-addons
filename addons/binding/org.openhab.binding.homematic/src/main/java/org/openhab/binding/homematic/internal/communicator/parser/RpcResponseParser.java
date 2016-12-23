/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.homematic.internal.communicator.client.UnknownRpcFailureException;
import org.openhab.binding.homematic.internal.communicator.message.RpcRequest;

/**
 * Parses the response from a RPC call, throws exception if fault response.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class RpcResponseParser extends CommonRpcParser<Object[], Object[]> {
    private RpcRequest response;

    public RpcResponseParser(RpcRequest request) {
        this.response = request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object[] parse(Object[] message) throws IOException {
        if (message != null && message.length > 0) {
            Object responseData = message[0];
            if (responseData instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) responseData;
                if (map.containsKey("faultCode")) {
                    Number faultCode = toNumber(map.get("faultCode"));
                    String faultString = toString(map.get("faultString"));
                    String faultMessage = String.format("%s %s (sending %s)", faultCode, faultString, response);
                    if (faultCode.intValue() == -1 && StringUtils.equals("Failure", faultString)) {
                        throw new UnknownRpcFailureException(faultMessage);
                    }
                    throw new IOException(faultMessage);
                }
            }
            return message;
        }
        throw new IOException("Unknown Result: " + message);
    }

}
