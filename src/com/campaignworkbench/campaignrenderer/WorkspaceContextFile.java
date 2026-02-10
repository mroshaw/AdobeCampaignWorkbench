package com.campaignworkbench.campaignrenderer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.nio.file.Path;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)

public class WorkspaceContextFile extends WorkspaceFile {
    private WorkspaceFile dataContextFile;

    public WorkspaceContextFile() {
        super();
    }

    public WorkspaceContextFile(Path filePath, WorkspaceFileType fileType) {
        super(filePath, fileType);
    }

    public void setDataContextFile(Path contextFilePath) {
        this.dataContextFile = new WorkspaceFile(contextFilePath, WorkspaceFileType.CONTEXT);
    }

    public void clearDataContext() {
        dataContextFile = null;
    }

    public boolean isDataContextSet() {
        return dataContextFile != null;
    }

    public WorkspaceFile getDataContextFile() {
        return dataContextFile;
    }

    public Path getDataContextFilePath() {
        return dataContextFile.getFilePath();
    }

    public Path getDataContextFileName() {
        return dataContextFile == null ? null : dataContextFile.getFileName();
    }

    public String getDataContextContent() {
        return dataContextFile.getWorkspaceFileContent();
    }
}
