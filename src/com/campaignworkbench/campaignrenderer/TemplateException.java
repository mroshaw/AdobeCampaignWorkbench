package com.campaignworkbench.campaignrenderer;

/**
 * Top level exception with detailed information about the process that caused the exception
 */
public abstract class TemplateException extends RuntimeException {
    private final String templateName;
    private final int templateLine;

    protected TemplateException(String message, String templateName, int templateLine, Throwable cause) {
        super(message, cause);
        this.templateName = templateName;
        this.templateLine = templateLine;
    }

    /**
     * Returns the name of the template that was running when the exception occurred
     * @return templateName as a string
     */
    public String getTemplateName() { return templateName; }

    /**
     * Returns the line number that caused the exception to occur
     * @return templateLine as an integer
     */
    public int getTemplateLine() { return templateLine; }
}
