package com.campaignworkbench.workspace;

/**
 * ENUM that contains meta-data about each type of workspace file.
 */
public enum WorkspaceFileType {
    TEMPLATE(
            "Templates",
            "Template File",
            "Template Files",
            "*.template"
    ),
    MODULE(
            "Modules",
            "Module File",
            "Module Files",
            "*.module"
    ),
    BLOCK(
            "Blocks",
            "Personalization Block File",
            "Block Files",
            "*.block"
    ),
    CONTEXT(
            "ContextXML",
            "Contexts",
            "XML Files",
            "*.xml"
    );

    // Used to derive paths
    private final String folderName;
    // Used to determine text to use in an 'Open File' dialog window
    private final String fileOpenWindowTitle;
    private final String fileOpenExtensionFilterDescription;
    private final String fileOpenExtensionFilter;

    WorkspaceFileType(
            String folderName,
            String fileOpenWindowTitle,
            String fileOpenExtensionFilterDescription,
            String fileOpenExtensionFilter) {

        this.fileOpenWindowTitle = fileOpenWindowTitle;
        this.folderName = folderName;
        this.fileOpenExtensionFilterDescription = fileOpenExtensionFilterDescription;
        this.fileOpenExtensionFilter = fileOpenExtensionFilter;
    }

    public String getFileOpenWindowTitle() {
        return fileOpenWindowTitle;
    }

    public String getFolderName() {
        return folderName;
    }

    public String extensionFilterDescription() {
        return fileOpenExtensionFilterDescription;
    }

    public String extensionFilter() {
        return fileOpenExtensionFilter;
    }

}
