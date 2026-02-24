package com.campaignworkbench.campaignrenderer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.nio.file.Path;

/**
 * Class representing a Template workspace file.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class Template extends WorkspaceContextFile {
    public Template(Path filePath) {
        super(filePath, WorkspaceFileType.TEMPLATE);
    }

    public Template() {}

    private WorkspaceFile messageContextFile;

    public void setMessageContextFile(Path contextFilePath) {
        messageContextFile = new WorkspaceFile(contextFilePath, WorkspaceFileType.CONTEXT);
    }

    public void clearMessageContext() {
        messageContextFile = null;
    }

    public boolean isMessageContextSet() {
        return messageContextFile != null;
    }

    public WorkspaceFile getMessageContextFile() {
        return messageContextFile;
    }

    public Path getMessageContextFilePath() {
        return messageContextFile.getFilePath();
    }

    public Path getMessageContextFileName() {
        return messageContextFile == null ? null : messageContextFile.getFileName();
    }

    public String getMessageContextContent() {
        return messageContextFile.getWorkspaceFileContent();
    }
}
