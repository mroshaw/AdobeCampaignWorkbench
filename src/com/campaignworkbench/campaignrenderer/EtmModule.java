package com.campaignworkbench.campaignrenderer;

import java.nio.file.Path;

public class EtmModule extends WorkspaceContextFile {
    public EtmModule(Path filePath) {
        super(filePath, Workspace.WorkspaceFileType.MODULE);
    }

    public EtmModule() {}
}
