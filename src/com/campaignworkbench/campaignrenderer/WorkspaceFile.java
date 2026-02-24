package com.campaignworkbench.campaignrenderer;
import com.campaignworkbench.util.FileUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.nio.file.Path;

/**
 * Class representing an Adobe Campaign specific file used in the workspace.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class WorkspaceFile {

    private Path workspaceFilePath;
    private WorkspaceFileType workspaceFileType;

    public WorkspaceFile() {
    }

    public WorkspaceFile(Path filePath, WorkspaceFileType fileType) {
        workspaceFilePath = filePath;
        workspaceFileType = fileType;
    }

    public Path getFileName() {
        return workspaceFilePath.getFileName();
    }

    public String getBaseFileName() {
        String fileName = workspaceFilePath.getFileName().toString(); // e.g. "example.txt"
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) { // dot exists and is not the first character
            return fileName.substring(0, dotIndex);
        } else {
            return fileName; // no extension found
        }
    }

    public WorkspaceFileType getWorkspaceFileType() {
        return workspaceFileType;
    }

    public boolean isTemplate() {
        return workspaceFileType == WorkspaceFileType.TEMPLATE;
    }

    public boolean isDataContextApplicable() {
        return workspaceFileType == WorkspaceFileType.TEMPLATE ||
            workspaceFileType == WorkspaceFileType.MODULE;
    }

    public boolean isMessageContextApplicable() {
        return workspaceFileType == WorkspaceFileType.TEMPLATE;
    }

    public Path getFilePath() {
        return workspaceFilePath;
    }

    public String getWorkspaceFileContent() {
        return getFileContent(workspaceFilePath);
    }

    protected String getFileContent(Path filePath) {
        return FileUtil.read(filePath);
    }
}
