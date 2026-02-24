package com.campaignworkbench.ide;

/**
 * High-level exception thrown by the user interface.
 */
public class IDEException extends RuntimeException {
    public IDEException(String message, Throwable cause) {
        super(message, cause);
    }
}
