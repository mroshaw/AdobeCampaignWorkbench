package com.campaignworkbench.workspace;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.nio.file.Path;

/**
 * Class representing a context XML workspace file.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class ContextXml extends WorkspaceFile {

    public ContextXml(String fileName, Workspace workspace) {
        super(fileName, WorkspaceFileType.CONTEXT, workspace);
    }

    public ContextXml() {}
}
