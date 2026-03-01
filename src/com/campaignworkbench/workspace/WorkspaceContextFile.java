package com.campaignworkbench.workspace;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.nio.file.Path;

/**
 * Class representing a workspace file that has a data XML context file associated with it.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class WorkspaceContextFile extends WorkspaceFile {
    private final ObjectProperty<ContextXml> dataContextFileProperty = new SimpleObjectProperty<>();
    private ContextXml dataContextFile;

    public WorkspaceContextFile() {
        super();
    }

    public WorkspaceContextFile(String fileName, WorkspaceFileType fileType, Workspace workspace) {
        super(fileName, fileType, workspace);
    }

    public void setDataContextFile(ContextXml dataContextFile) {
        // System.out.println("Template instance: " + this);
        // System.out.println("Old value: " + getDataContextFile());
        // System.out.println("New value: " + dataContextFile);
        this.dataContextFile = dataContextFile;
        dataContextFileProperty.setValue(dataContextFile);

    }

    public void clearDataContext() {
        setDataContextFile(null);
    }

    public boolean isDataContextSet() {
        return dataContextFile != null;
    }

    public ContextXml getDataContextFile() {
        return dataContextFile;
    }

    public Path getDataContextAbsoluteFilePath() {
        return dataContextFile.getAbsoluteFilePath();
    }

    public String getDataContextContent() {
        return dataContextFile.getWorkspaceFileContent();
    }

    public ObjectProperty<ContextXml> getDataContextFileProperty() { return dataContextFileProperty; }
}
