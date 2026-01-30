package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.ide.IDEException;
import com.campaignworkbench.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class to support a working area with appropriate files
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class Workspace {

    private static final String workspacesRootName = "Workspaces";

    public enum WorkspaceFileType {TEMPLATE, MODULE, BLOCK, CONTEXT}

    private Path configFilePath;
    private Path rootFolderPath;
    private ArrayList<Template> templates;
    private ArrayList<EtmModule> modules;
    private ArrayList<PersonalisationBlock> blocks;
    private ArrayList<ContextXml> contexts;

    /**
     * List of required subfolders in a valid workspace
     */
    public static final List<String> REQUIRED =
            List.of("Templates", "Modules", "Blocks", "ContextXml");

    /**
     * @param workspaceFilePath path to the workspace JSON for the workspace
     */
    public Workspace(Path workspaceFilePath, boolean createNew) {

        Path workspacesRootPath = getWorkspacesRootPath();
        // Create 'Workspaces' root, if not already present
        if(!Files.exists(workspacesRootPath)) {
            try {
                Files.createDirectory(workspacesRootPath);
            } catch (IOException ioe) {
                throw new IDEException("An error occurred creating Workspaces root: " + workspacesRootPath, ioe.getCause());
            }
        }

        configFilePath = workspaceFilePath;
        rootFolderPath = configFilePath.getParent();

        templates = new ArrayList<>();
        modules = new ArrayList<>();
        blocks = new ArrayList<>();
        contexts = new ArrayList<>();

        if (createNew) {
            createNewWorkspace();
        }
    }

    public Workspace() {
    }

    private void createWorkspaceRootFolder() {

    }

    public static Path getWorkspacesRootPath() {
        File userDir = new File(System.getProperty("user.dir"));
        return userDir.toPath().resolve(workspacesRootName);
    }

    public void createNewWorkspace() {

        // Create folder structure
        // Derive workspace folders
        Path templateFolder = rootFolderPath.resolve("Templates");
        Path moduleFolder = rootFolderPath.resolve("Modules");
        Path blocksFolder = rootFolderPath.resolve("Blocks");
        Path contextFolder = rootFolderPath.resolve("ContextXml");

        if (Files.exists(configFilePath) || Files.exists(templateFolder) || Files.exists(moduleFolder) || Files.exists(blocksFolder) || Files.exists(contextFolder)) {
            throw new IDEException("Invalid location selected for new workspace. Workspace files already exists!", null);
        }

        try {
            Files.createDirectory(templateFolder);
            Files.createDirectory(moduleFolder);
            Files.createDirectory(blocksFolder);
            Files.createDirectory(contextFolder);

        } catch (IOException ioe) {
            throw new IDEException("An error occurred creating the new workspace: " + configFilePath, ioe.getCause());
        }

        // Save config file
        writeToJson(configFilePath);
    }

    /**
     * @return path of the workspace root
     */
    public Path getRootFolderPath() {
        return rootFolderPath;
    }

    /**
     * @return full path to the workspace Templates folder
     */
    public Path getTemplatesPath() {
        return rootFolderPath.resolve("Templates");
    }

    /**
     * @return full path to the workspace Modules folder
     */
    public Path getModulesPath() {
        return rootFolderPath.resolve("Modules");
    }

    /**
     * @return full path to the workspace Blocks folder
     */
    public Path getBlocksPath() {
        return rootFolderPath.resolve("Blocks");
    }

    /**
     * @return full path to the workspace ContextXml folder
     */
    public Path getContextXmlPath() {
        return rootFolderPath.resolve("ContextXml");
    }

    /**
     * Determine if the workspace work is valid
     *
     * @return true if valid, otherwise false
     */
    public boolean isValid() {
        return REQUIRED.stream()
                .allMatch(name -> rootFolderPath.resolve(name).toFile().isDirectory());
    }

    /**
     * @param subfolder from which files should be listed
     * @return list of files in the subfolder
     */
    public List<File> getFolderFiles(String subfolder) {
        File dir = rootFolderPath.resolve(subfolder).toFile();
        return dir.isDirectory()
                ? Arrays.stream(dir.listFiles()).toList()
                : List.of();
    }

    /**
     * @return all files in the current workspace
     */
    public List<File> getAllFiles() {
        return REQUIRED.stream()
                .flatMap(name -> {
                    File d = rootFolderPath.resolve(name).toFile();
                    return d.isDirectory()
                            ? Arrays.stream(d.listFiles())
                            : Stream.empty();
                })
                .toList();
    }


    /**
     * @param workspaceRootPath Root path of the workspace JSON file
     */

    public void openWorkspace(Path workspaceRootPath) {
        File workspaceJsonFile = rootFolderPath.resolve(workspaceRootPath).toFile();
        readFromJson(workspaceRootPath);
    }

    public void addNewWorkspaceFile(Path filePath, WorkspaceFileType fileType) throws IDEException {

        if(fileExists(filePath, fileType)) {
            return;
        }

        try {
            Path newFilePath = Files.createFile(filePath);
            addExistingWorkspaceFile(newFilePath, fileType);
        } catch (IOException ioe) {
            throw new IDEException("An error occurred adding a new file to the workspace: " + filePath, ioe.getCause());
        }
    }

    public void addExistingWorkspaceFile(Path filePath, WorkspaceFileType fileType) {

        if(fileExists(filePath, fileType)) {
            return;
        }

        switch (fileType) {
            case TEMPLATE:
                Template newTemplateFile = new Template(filePath);
                templates.add(newTemplateFile);
                break;

            case MODULE:
                EtmModule newModule = new EtmModule(filePath);
                modules.add(newModule);
                break;

            case BLOCK:
                PersonalisationBlock newBlock = new PersonalisationBlock(filePath);
                blocks.add(newBlock);
                break;

            case CONTEXT:
                ContextXml newContext = new ContextXml(filePath);
                contexts.add(newContext);
                break;
        }

    }

    private boolean fileExists(Path filePath, WorkspaceFileType fileType) {

        return switch (fileType) {
            case TEMPLATE -> getTemplate(filePath.toString()).isPresent();
            case MODULE -> getEtmModule(filePath.toString()).isPresent();
            case BLOCK -> getBlock(filePath.toString()).isPresent();
            case CONTEXT -> getContext(filePath.toString()).isPresent();
        };
    }

    private Optional<Template> getTemplate(String templateFilePath) {
        return templates.stream()
                .filter(template -> templateFilePath.equals(template.getFilePath().toString()))
                .findFirst();
    }

    private Optional<PersonalisationBlock> getBlock(String blockFilePath) {
        return blocks.stream()
                .filter(block -> blockFilePath.equals(block.getFilePath().toString()))
                .findFirst();
    }

    private Optional<ContextXml> getContext(String contextFilePath) {
        return contexts.stream()
                .filter(module -> contextFilePath.equals(module.getFilePath().toString()))
                .findFirst();
    }

    public Optional<EtmModule> getEtmModule(String moduleFilePath) {
        return modules.stream()
                .filter(module -> moduleFilePath.equals(module.getFilePath().toString()))
                .findFirst();
    }

    public Optional<EtmModule> getEtmModuleByName(String moduleName) {
        moduleName += ".module";
        String finalModuleName = moduleName;
        return modules.stream()
                .filter(module -> finalModuleName.equals(module.getFileName().toString()))
                .findFirst();
    }

    /**
     * Writes the workspace configuration to a JSON file
     *
     * @param jsonFilePath full path to the workspace JSON file
     */
    public void writeToJson(Path jsonFilePath) {
        try {
            JsonUtil.writeToJson(jsonFilePath, this);
        } catch (IOException ioe) {
            throw new IDEException("An error occurred saving the workspace JSON file: " + jsonFilePath, ioe.getCause());
        } catch (Exception e) {
            throw new IDEException("An unknown occurred saving the workspace JSON file: " + jsonFilePath.toString(), null);
        }
    }


    /**
     * Overload to save current workspace file
     */
    public void writeToJson() {
        writeToJson(configFilePath);
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public List<EtmModule> getModules() {
        return modules;
    }

    public List<PersonalisationBlock> getBlocks() {
        return blocks;
    }

    public List<ContextXml> getContexts() {
        return contexts;
    }

    /**
     * Reads a workspace configuration from a JSON file
     *
     * @param jsonFilePath full path to the workspace JSON file
     */
    public void readFromJson(Path jsonFilePath) {
        try {
            Workspace newWorkspace = JsonUtil.readFromJson(jsonFilePath, Workspace.class);

            // Update the current workspace with new values
            this.rootFolderPath = newWorkspace.rootFolderPath;
            this.configFilePath = newWorkspace.configFilePath;

            this.templates = newWorkspace.templates;
            this.modules = newWorkspace.modules;
            this.blocks = newWorkspace.blocks;
            this.contexts = newWorkspace.contexts;

        } catch (IOException ioe) {
            throw new IDEException("An error occurred loading the workspace JSON file: " + jsonFilePath, ioe.getCause());
        } catch (Exception e) {
            throw new IDEException("An unknown occurred loading the workspace JSON file: " + jsonFilePath.toString(), e);
        }
    }
}
