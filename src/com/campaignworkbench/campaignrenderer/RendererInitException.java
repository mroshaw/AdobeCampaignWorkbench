package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.workspace.WorkspaceFile;

/**
 * JavaScript/JSP parser specific exceptions
 */
public final class RendererInitException extends RendererException {
    /**
     * Template parsing exception
     * @param msg describing the exception
     * @param workspaceFile with the name of the template being executed
     * @param sourceCode source code being parsed when the exception occurred
     * @param line number on which the exception occurred
     * @param rootCause root cause of the error
     * @param solution recommended solution
     * @param cause of the underlying exception
     */
    public RendererInitException(String msg, WorkspaceFile workspaceFile, String sourceCode, int line, String rootCause, String solution, Throwable cause) {
        super(msg, workspaceFile, sourceCode, line, rootCause, solution, cause);
    }
}
