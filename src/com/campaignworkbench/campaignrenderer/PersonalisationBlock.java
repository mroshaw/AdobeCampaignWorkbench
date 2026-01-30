package com.campaignworkbench.campaignrenderer;

import java.nio.file.Path;

public class PersonalisationBlock extends WorkspaceFile {
    public PersonalisationBlock(Path filePath) {
        super(filePath, Workspace.WorkspaceFileType.BLOCK);
    }

    public PersonalisationBlock() {}
}
