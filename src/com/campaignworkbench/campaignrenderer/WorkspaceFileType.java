package com.campaignworkbench.campaignrenderer;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * ENUM that contains meta-data about each type of workspace file.
 */
public enum WorkspaceFileType {
    TEMPLATE(
        "Template File",
        Workspace::getTemplatesPath,
        "Template Files",
                "*.template"
    ),
    MODULE(
        "Module File",
        Workspace::getModulesPath,
        "Module Files",
                "*.module"
    ),
    BLOCK(
        "Personalization Block File",
        Workspace::getBlocksPath,
        "Block Files",
                "*.block"
    ),
    CONTEXT(
        "Context XML File",
        Workspace::getContextXmlPath,
        "XML Files",
                "*.xml"
    );

    /* Used to determine text to use in an 'Open File' dialog window */
    private final String fileOpenWindowTitle;
    private final Function<Workspace, Path> initialDirectoryProvider;
    private final String fileOpenExtensionFilterDescription;
    private final String fileOpenExtensionFilter;

    WorkspaceFileType(
            String fileOpenWindowTitle,
            Function<Workspace, Path> initialDirectoryProvider,
            String fileOpenExtensionFilterDescription,
            String fileOpenExtensionFilter) {

        this.fileOpenWindowTitle = fileOpenWindowTitle;
        this.initialDirectoryProvider = initialDirectoryProvider;
        this.fileOpenExtensionFilterDescription = fileOpenExtensionFilterDescription;
        this.fileOpenExtensionFilter = fileOpenExtensionFilter;
    }

    public String getFileOpenWindowTitle() {
        return fileOpenWindowTitle;
    }

    public Path initialDirectory(Workspace workspace) {
        return initialDirectoryProvider.apply(workspace);
    }

    public String extensionFilterDescription() {
        return fileOpenExtensionFilterDescription;
    }

    public String extensionFilter() {
        return fileOpenExtensionFilter;
    }

}
