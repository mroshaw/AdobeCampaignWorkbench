package com.campaignworkbench.workspace;
import com.campaignworkbench.ide.IdeException;
import com.campaignworkbench.util.FileUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private String fileName;
    private WorkspaceFileType fileType;
    @JsonIgnore
    private Workspace workspace;

    public WorkspaceFile() {
    }

    public WorkspaceFile(String fileName, WorkspaceFileType fileType, Workspace workspace) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.workspace = workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * @return file name without the extension
     */
    public String getBaseFileName() {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        } else {
            return fileName; // no extension found
        }
    }

    public Path getRelativeFilePath() {
        return Paths.get(fileType.getFolderName(), fileName);
    }

    public Path getAbsoluteFilePath() {
        return workspace.getRootFolderPath().resolve(getRelativeFilePath());
    }

    public WorkspaceFileType getFileType() {
        return fileType;
    }

    public boolean isTemplate() {
        return fileType == WorkspaceFileType.TEMPLATE;
    }

    public boolean isDataContextApplicable() {
        return fileType == WorkspaceFileType.TEMPLATE ||
                fileType == WorkspaceFileType.MODULE;
    }

    public boolean isMessageContextApplicable() {
        return fileType == WorkspaceFileType.TEMPLATE;
    }

    public String getWorkspaceFileContent() throws IdeException {
        return getFileContent(getAbsoluteFilePath());
    }

    /**
     * @return string content of the given file. To be used by this and derived classes.
     */
    protected String getFileContent(Path absoluteFilePath) throws IdeException {
        try {
            return FileUtil.read(absoluteFilePath);
        } catch (RuntimeException rte) {
            throw new IdeException("An error occurred loading the file: " + absoluteFilePath, rte);
        }
    }

    public void saveWorkspaceFileContent(String contentText) throws IdeException {
        saveFileContent(getAbsoluteFilePath(), contentText);
    }

    public void saveFileContent(Path absoluteFilePath, String contentText) {
        try {
            FileUtil.write(absoluteFilePath, contentText);
        } catch (RuntimeException rte) {
            throw new IdeException("An error occurred saving the file: " + absoluteFilePath, rte);
        }
    }

    public void deleteFromFileSystem() throws IdeException{
        Path deleteFileAbsolutePath = getAbsoluteFilePath();
        System.out.println("Deleting: " + deleteFileAbsolutePath);
        try {
            Files.delete(deleteFileAbsolutePath);
        } catch (IOException ioe) {
            throw new IdeException("An error occurred deleting the file from the file system: " + deleteFileAbsolutePath, ioe.getCause());
        }
    }
}
