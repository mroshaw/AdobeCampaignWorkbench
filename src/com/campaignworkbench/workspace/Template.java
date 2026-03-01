package com.campaignworkbench.workspace;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

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
    private final ObjectProperty<ContextXml> messageContextFileProperty = new SimpleObjectProperty<>();
    private ContextXml messageContextFile;

    public Template(String fileName, Workspace workspace) {
        super(fileName, WorkspaceFileType.TEMPLATE, workspace);
    }

    public Template() {}

    public void setMessageContextFile(ContextXml messageContextFile) {

        // System.out.println("Template instance: " + this);
        // System.out.println("Old value: " + getMessageContextFile());
        // System.out.println("New value: " + messageContextFile);

        this.messageContextFile = messageContextFile;
        messageContextFileProperty.setValue(messageContextFile);
    }

    public void clearMessageContext() {
        setMessageContextFile(null);
    }

    public boolean isMessageContextSet() {
        return messageContextFile != null;
    }

    public WorkspaceFile getMessageContextFile() {
        return messageContextFile;
    }

    public Path getMessageContextAbsoluteFilePath() {
        return messageContextFile.getAbsoluteFilePath();
    }

    public String getMessageContextFileName() {
        return messageContextFile == null ? null : messageContextFile.getFileName();
    }

    public String getMessageContextContent() {
        return messageContextFile.getWorkspaceFileContent();
    }

    public ObjectProperty<ContextXml> getMessageContextFileProperty() { return messageContextFileProperty; }

}
