package com.campaignworkbench.ide;

/**
 * High-level exception thrown by the user interface.
 */
public class IdeException extends RuntimeException {
    public IdeException(String message, Throwable cause) {
        super(message, cause);
    }
}
