package com.campaignworkbench.ide;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to support a working area with appropriate files
 */
public class Workspace {

    private final Path root;
    public static final List<String> REQUIRED =
            Arrays.asList("Templates", "Modules", "Blocks", "ContextXml");

    /**
     * @param root folder for the workspace
     */
    public Workspace(Path root) {
        this.root = root;
    }

    /**
     * @return path of the workspace root
     */
    public Path getRoot() {
        return root;
    }

    /**
     * @return full path to the workspace Templates folder
     */
    public Path getTemplatesPath() {
        return root.resolve("Templates");
    }

    /**
     * @return full path to the workspace Modules folder
     */
    public Path getModulesPath() {
        return root.resolve("Modules");
    }

    /**
     * @return full path to the workspace Blocks folder
     */
    public Path getBlocksPath() {
        return root.resolve("Blocks");
    }

    /**
     * @return full path to the workspace ContextXml folder
     */
    public Path getContextXmlPath() {
        return root.resolve("ContextXml");
    }

    /**
     * Determine if the workspace work is valid
     * @return true if valid, otherwise false
     */
    public boolean isValid() {
        return REQUIRED.stream()
                .allMatch(name -> root.resolve(name).toFile().isDirectory());
    }

    /**
     * @param subfolder from which files should be listed
     * @return list of files in the subfolder
     */
    public List<File> getFolderFiles(String subfolder) {
        File dir = root.resolve(subfolder).toFile();
        return dir.isDirectory()
                ? Arrays.stream(dir.listFiles()).collect(Collectors.toList())
                : List.of();
    }

    /**
     * @return all files in the current workspace
     */
    public List<File> getAllFiles() {
        return REQUIRED.stream()
                .flatMap(name -> {
                    File d = root.resolve(name).toFile();
                    return d.isDirectory()
                            ? Arrays.stream(d.listFiles())
                            : Stream.empty();
                })
                .collect(Collectors.toList());
    }
}
