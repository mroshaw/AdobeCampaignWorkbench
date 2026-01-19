package com.campaignworkbench.util;

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
}
