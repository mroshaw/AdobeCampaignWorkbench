package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.workspace.WorkspaceFile;

/**
 * JavaScript generation-specific exceptions
 */
public final class RendererGenerationException extends RendererException {
    /**
     * Template generation exception
     * @param msg describing the exception
     * @param workspaceFile with the name of the template being executed
     * @param sourceCode source code being processed when the exception occurred
     * @param line number on which the exception occurred
     * @param rootCause root cause of the error
     * @param solution recommended solution
     * @param cause of the underlying exception
     */
    public RendererGenerationException(String msg, WorkspaceFile workspaceFile, String sourceCode, int line, String rootCause, String solution, Throwable cause) {
        super(msg, workspaceFile, sourceCode, line, rootCause, solution, cause);
    }
}
