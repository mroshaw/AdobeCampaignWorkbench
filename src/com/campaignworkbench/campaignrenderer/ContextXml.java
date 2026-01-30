package com.campaignworkbench.campaignrenderer;

import java.nio.file.Path;

public class ContextXml extends WorkspaceFile {

    public ContextXml(Path filePath) {
        super(filePath, Workspace.WorkspaceFileType.CONTEXT);
    }

    public ContextXml() {}
}
