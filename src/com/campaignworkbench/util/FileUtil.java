package com.campaignworkbench.util;

import com.campaignworkbench.campaignrenderer.Workspace;
import com.campaignworkbench.campaignrenderer.WorkspaceFileType;
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

    public static File openFile(Workspace workspace, WorkspaceFileType fileType, Window owner) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(fileType.title());
        fileChooser.setInitialDirectory(fileType.initialDirectory(workspace).toFile());
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
}
