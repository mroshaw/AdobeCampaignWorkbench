package com.campaignworkbench.campaignrenderer;

/**
 * JavaScript generation specific exceptions
 */
public final class TemplateGenerationException extends TemplateException {
    /**
     * Template generation exception
     * @param msg describing the exception
     * @param template with the name of the template being executed
     * @param sourceCode source code being processed when the exception occurred
     * @param line number on which the exception occurred
     * @param rootCause root cause of the error
     * @param solution recommended solution
     * @param cause of the underlying exception
     */
    public TemplateGenerationException(String msg, String template, String sourceCode, int line, String rootCause, String solution, Throwable cause) {
        super(msg, template, sourceCode, line, rootCause, solution, cause);
    }
}
