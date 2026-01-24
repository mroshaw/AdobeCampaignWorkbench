package com.campaignworkbench.ide.editor;

/**
 * Selection of available syntax types for the code editor
 */
public enum SyntaxType {
    /**
     * JSP like
     */
    TEMPLATE,
    /**
     * JavaScript like
     */
    BLOCK,
    /**
     * Plain XML
     */
    XML,
    /**
     * JSP like for modules
     */
    MODULE,
    /**
     * JSP like for source preview
     */
    SOURCE_PREVIEW,
    /**
     * HTML highlighting
     */
    HTML_PREVIEW,
    /**
     * Plain text, no highlighting
     */
    PLAIN
}
