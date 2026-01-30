package com.campaignworkbench.campaignrenderer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.nio.file.Path;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class WorkspaceContextFile extends WorkspaceFile {
    private Path contextFilePath;

    public WorkspaceContextFile() { super(); }

    public WorkspaceContextFile(Path filePath, Workspace.WorkspaceFileType fileType) {
        super(filePath, fileType);
    }

    public void setXmlContextFile(Path contextFilePath) {
        this.contextFilePath = contextFilePath;
    }

    public void clearContext() {
        contextFilePath = null;
    }

    public boolean isContextSet() {
        return contextFilePath != null;
    }

    public Path getContextFilePath() {
        return contextFilePath;
    }

    public Path getContextFileName() {
        return contextFilePath.getFileName();
    }

    public String getContextContent() {
        return super.getFileContent(contextFilePath);
    }
}
