package org.openhab.binding.mysensors.internal.exception;

public class MergeException extends Exception {

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
