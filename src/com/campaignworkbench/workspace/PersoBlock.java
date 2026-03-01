package com.campaignworkbench.workspace;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.nio.file.Path;

/**
 * Class representing a Personaliszation Block workspace file.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)

public class PersoBlock extends WorkspaceFile {
    public PersoBlock(String fileName, Workspace workspace) {
        super(fileName, WorkspaceFileType.BLOCK, workspace);
    }

    public PersoBlock() {}
}
