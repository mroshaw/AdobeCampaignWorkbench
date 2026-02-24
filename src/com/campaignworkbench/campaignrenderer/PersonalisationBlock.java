package com.campaignworkbench.campaignrenderer;

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

public class PersonalisationBlock extends WorkspaceFile {
    public PersonalisationBlock(Path filePath) {
        super(filePath, WorkspaceFileType.BLOCK);
    }

    public PersonalisationBlock() {}
}
