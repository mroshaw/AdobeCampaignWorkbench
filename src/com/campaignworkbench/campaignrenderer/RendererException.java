package com.campaignworkbench.campaignrenderer;

/**
 * Top level exception with detailed information about the process that caused the exception
 */
public abstract sealed class RendererException extends RuntimeException permits RendererExecutionException, RendererGenerationException, RendererParseException {
    private final WorkspaceFile sourceFile;
    private final String sourceCode;
    private final int templateLine;
    private final String rootCause;
    private final String solution;

    protected RendererException(String message, WorkspaceFile sourceFile, String sourceCode, int templateLine, String rootCause, String solution, Throwable cause) {
        super(message, cause);
        this.sourceFile = sourceFile;
        this.sourceCode = sourceCode;
        this.templateLine = templateLine;
        this.rootCause = rootCause;
        this.solution = solution;
    }

    /**
     * Returns the WorkfspaceFile that generated the exception
     * @return sourceFile as a WorkspaceFile
     */
    public WorkspaceFile getWorkspaceFile() { return sourceFile; }

    /**
     * Returns the line number that caused the exception to occur
     * @return templateLine as an integer
     */
    public int getTemplateLine() { return templateLine; }

    /**
     * Returns the code being processed when the exception occurs
     * @return source code
     */
    public String getSourceCode() { return sourceCode; }

    /**
     * Returns the root cause of the error
     * @return root cause
     */
    public String getRootCause() { return rootCause; }

    /**
     * Returns a recommended solution
     * @return solution
     */
    public String getSolution() { return solution; }
}
