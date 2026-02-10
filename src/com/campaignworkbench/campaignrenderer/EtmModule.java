package com.campaignworkbench.campaignrenderer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.nio.file.Path;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class EtmModule extends WorkspaceContextFile {
    public EtmModule(Path filePath) {
        super(filePath, WorkspaceFileType.MODULE);
    }

    public EtmModule() {}
}
