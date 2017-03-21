/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.exception;

public class MergeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 6237378516242187660L;

    private String message;

    public MergeException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
