package com.campaignworkbench.campaignrenderer;

/**
 * Execution specific exceptions
 */
public final class TemplateExecutionException extends TemplateException {
    /**
     * Template execution exception
     * @param msg describing the exception
     * @param template with the name of the template being executed
     * @param line number on which the exception occurred
     * @param cause of the underlying exception
     */
    public TemplateExecutionException(String msg, String template, int line, Throwable cause) {
        super(msg, template, line, cause);
    }
}
