package com.campaignworkbench.campaignrenderer;

import java.nio.file.Path;

public class WorkspaceContextFile extends WorkspaceFile {
    private Path dataContextFilePath;

    public WorkspaceContextFile() { super(); }

    public WorkspaceContextFile(Path filePath, Workspace.WorkspaceFileType fileType) {
        super(filePath, fileType);
    }

    public void setDataContextFile(Path contextFilePath) {
        this.dataContextFilePath = contextFilePath;
    }

    public void clearDataContext() {
        dataContextFilePath = null;
    }

    public boolean isDataContextSet() {
        return dataContextFilePath != null;
    }

    public Path getDataContextFilePath() {
        return dataContextFilePath;
    }

    public Path getDataContextFileName() {
        return dataContextFilePath == null ? null : dataContextFilePath.getFileName();
    }

    public String getDataContextContent() {
        return super.getFileContent(dataContextFilePath);
    }
}
