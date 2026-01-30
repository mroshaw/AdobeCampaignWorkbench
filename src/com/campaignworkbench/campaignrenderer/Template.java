package com.campaignworkbench.campaignrenderer;

import java.nio.file.Path;

public class Template extends WorkspaceContextFile {
    public Template(Path filePath) {
        super(filePath, Workspace.WorkspaceFileType.TEMPLATE);
    }

    public Template() {}
}
