package com.campaignworkbench.ide.editor.richtextfx;

/**
 * Interface for formatting source code.
 */
public interface ICodeFormatter {
    String format(String unformattedCode, int indent);
}
