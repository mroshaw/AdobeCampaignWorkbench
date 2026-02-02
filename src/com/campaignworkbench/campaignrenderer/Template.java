package com.campaignworkbench.campaignrenderer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.nio.file.Path;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class Template extends WorkspaceContextFile {
    public Template(Path filePath) {
        super(filePath, Workspace.WorkspaceFileType.TEMPLATE);
    }

    public Template() {}

    private Path messageContextFilePath;

    public void setMessageContextFile(Path contextFilePath) {
        messageContextFilePath = contextFilePath;
    }

    public void clearMessageContext() {
        messageContextFilePath = null;
    }

    public boolean isMessageContextSet() {
        return messageContextFilePath != null;
    }

    public Path getMessageContextFilePath() {
        return messageContextFilePath;
    }

    public Path getMessageContextFileName() {
        return messageContextFilePath == null ? null : messageContextFilePath.getFileName();
    }

    public String getMessageContextContent() {
        return super.getFileContent(messageContextFilePath);
    }
}
