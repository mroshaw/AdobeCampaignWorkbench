package com.campaignworkbench.campaignrenderer;

import java.nio.file.Path;
import java.util.function.Function;

public enum WorkspaceFileType {
    TEMPLATE(
        "Open Template File",
        Workspace::getTemplatesPath,
        "Template Files",
                "*.template"
    ),
    MODULE(
        "Open Module File",
        Workspace::getModulesPath,
        "Module Files",
                "*.module"
    ),
    BLOCK(
        "Open Perso Block File",
        Workspace::getBlocksPath,
        "Block Files",
                "*.block"
    ),
    CONTEXT(
        "Open Context XML File",
        Workspace::getContextXmlPath,
        "XML Files",
                "*.xml"
    );

    private final String title;
    private final Function<Workspace, Path> initialDirectoryProvider;
    private final String extensionFilterDescription;
    private final String extensionFilter;

    WorkspaceFileType(
            String title,
            Function<Workspace, Path> initialDirectoryProvider,
            String extensionFilterDescription,
            String extensionFilter) {

        this.title = title;
        this.initialDirectoryProvider = initialDirectoryProvider;
        this.extensionFilterDescription = extensionFilterDescription;
        this.extensionFilter = extensionFilter;
    }

    public String title() {
        return title;
    }

    public Path initialDirectory(Workspace workspace) {
        return initialDirectoryProvider.apply(workspace);
    }

    public String extensionFilterDescription() {
        return extensionFilterDescription;
    }

    public String extensionFilter() {
        return extensionFilter;
    }

}
