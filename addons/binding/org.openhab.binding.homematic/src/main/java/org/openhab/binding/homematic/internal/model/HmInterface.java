/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.model;

/**
 * Definition of the Homematic interfaces.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public enum HmInterface {
    RF,
    WIRED,
    HMIP,
    CUXD;

    /**
     * Returns the full name of the interface.
     */
    public String getName() {
        switch (this) {
            case RF:
                return "BidCos-RF";
            case WIRED:
                return "BidCos-Wired";
            case HMIP:
                return "HmIP-RF";
            case CUXD:
                return "CUxD";
        }
        return null;
    }
}
