package com.campaignworkbench.util;

import com.campaignworkbench.ide.FileChooserConfig;
import com.campaignworkbench.workspace.Workspace;
import com.campaignworkbench.workspace.WorkspaceFileType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides methods for managing files
 */
public final class FileUtil {

    private FileUtil() {}

    /**
     * Reads the entire content of a file as a string (UTF-8)
     * @param path full path to the file to read
     * @return string of file content
     */
    public static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    public static void write(Path path, String contentText) {
        try {
            byte[] strToBytes = contentText.getBytes();
            Files.write(path, strToBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + path, e);
        }
    }

    public static File openFile(Workspace workspace, WorkspaceFileType fileType, String action,  Window owner) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(action + " " + fileType.getFileOpenWindowTitle());
        fileChooser.setInitialDirectory(workspace.getRootFolderPath().resolve(fileType.getFolderName()).toFile());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(fileType.extensionFilterDescription(), fileType.extensionFilter())
        );

        File selectedFile = fileChooser.showOpenDialog(owner);

        String verifyFileExtension = fileType.extensionFilter().substring(1);

        if (selectedFile != null && selectedFile.getName().endsWith(verifyFileExtension)) {

            return selectedFile;
        }
        return null;

    }

    public static FileChooserConfig getFileChooserConfig(WorkspaceFileType fileType, Path workspaceRootPath, String action) {
        return switch (fileType) {
            case TEMPLATE -> new FileChooserConfig(
                    action + " template file",
                    workspaceRootPath.resolve(fileType.getFolderName()).toFile(),
                    "Template files",
                    "*.template"
            );
            case MODULE -> new FileChooserConfig(
                    action + " module file",
                    workspaceRootPath.resolve(fileType.getFolderName()).toFile(),
                    "ETM Module files",
                    "*.module"
            );
            case BLOCK -> new FileChooserConfig(
                    action + " block file",
                    workspaceRootPath.resolve(fileType.getFolderName()).toFile(),
                    "Block files",
                    "*.block"
            );
            case CONTEXT -> new FileChooserConfig(
                    action + " context file",
                    workspaceRootPath.resolve(fileType.getFolderName()).toFile(),
                    "Context XML files",
                    "*.xml"
            );
        };
    }
}
