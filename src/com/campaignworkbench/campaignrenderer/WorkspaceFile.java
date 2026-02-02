package com.campaignworkbench.campaignrenderer;
import com.campaignworkbench.util.FileUtil;

import java.nio.file.Path;

public class WorkspaceFile {

    private Path workspaceFilePath;

    private Workspace.WorkspaceFileType workspaceFileType;

    public WorkspaceFile() {
    }

    public WorkspaceFile(Path filePath, Workspace.WorkspaceFileType fileType) {
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

    public Workspace.WorkspaceFileType getWorkspaceFileType() {
        return workspaceFileType;
    }

    public boolean isTemplate() {
        return workspaceFileType == Workspace.WorkspaceFileType.TEMPLATE;
    }

    public boolean isDataContextApplicable() {
        return workspaceFileType == Workspace.WorkspaceFileType.TEMPLATE ||
            workspaceFileType == Workspace.WorkspaceFileType.MODULE;
    }

    public boolean isMessageContextApplicable() {
        return workspaceFileType == Workspace.WorkspaceFileType.TEMPLATE;
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
