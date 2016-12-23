/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.processor;

/**
 * ISS tag types enumeration.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public enum TagType {

    LABEL("label", false),
    ROOM("room", false),
    TYPE("type", false),
    MAPPING("mapping", false),
    LINK("link", true),
    UNIT("unit", false),
    INVERT("invert", false);

    private final String prefix;
    private final boolean multiValue;

    TagType(String prefix, boolean multiValue) {
        this.prefix = prefix;
        this.multiValue = multiValue;
    }

    /**
     * @return Tag prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return True if this tag may exist multiple times on a single item.
     */
    public boolean isMultiValue() {
        return multiValue;
    }

}
