package com.campaignworkbench.campaignrenderer;

/**
 * JavaScript/JSP parser specific exceptions
 */
public final class TemplateParseException extends TemplateException {
    /**
     * Template parsing exception
     * @param msg describing the exception
     * @param template with the name of the template being executed
     * @param line number on which the exception occurred
     * @param cause of the underlying exception
     */
    public TemplateParseException(String msg, String template, int line, Throwable cause) {
        super(msg, template, line, cause);
    }
}
