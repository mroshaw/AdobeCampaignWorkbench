package com.campaignworkbench.campaignrenderer;

/**
 * Execution specific exceptions
 */
public final class RendererExecutionException extends RendererException {

    /**
     * Template execution exception
     * @param msg describing the exception
     * @param workspaceFile with the name of the template being executed
     * @param sourceCode source code that triggered the exception
     * @param line number on which the exception occurred
     * @param rootCause root cause of the error
     * @param solution recommended solution
     * @param cause of the underlying exception
     */
    public RendererExecutionException(String msg, WorkspaceFile workspaceFile, String sourceCode, int line, String rootCause, String solution, Throwable cause) {
        super(msg, workspaceFile, sourceCode, line, rootCause, solution, cause);
    }

}
