package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.ide.IDEException;
import com.campaignworkbench.ide.LogPanel;
import com.campaignworkbench.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class to support a working area with appropriate files
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class Workspace {

    private static final String workspacesRootName = "Campaign Workbench Workspaces";

    @JsonProperty
    private Path configFilePath;
    @JsonProperty
    private Path rootFolderPath;

    @JsonIgnore
    private final ObservableList<Template> templates =
            FXCollections.observableArrayList();

    @JsonIgnore
    private final ObservableList<EtmModule> modules =
            FXCollections.observableArrayList();

    @JsonIgnore
    private final ObservableList<PersonalisationBlock> blocks =
            FXCollections.observableArrayList();

    @JsonIgnore
    private final ObservableList<ContextXml> contexts =
            FXCollections.observableArrayList();

    /**
     * @param workspaceFilePath path to the workspace JSON for the workspace
     *
     */
    public Workspace(Path workspaceFilePath, boolean createNew) {

        configFilePath = workspaceFilePath;
        rootFolderPath = configFilePath.getParent();

        if (createNew) {
            createNewWorkspace();
        } else {
            openWorkspace(configFilePath);
        }
    }

    public Workspace() {
    }

    public static void createWorkspaceRootFolder() {
        Path workspacesRootPath = getWorkspacesRootPath();
        // Create a 'Workspaces' root, if not already present
        if (!Files.exists(workspacesRootPath)) {
            try {
                System.out.println("Creating Workspaces root: " + workspacesRootPath);
                Files.createDirectory(workspacesRootPath);
                System.out.println("Created Workspaces root: " + workspacesRootPath);
            } catch (IOException ioe) {
                throw new IDEException("An error occurred creating Workspaces root: " + workspacesRootPath, ioe.getCause());
            }
        } else {
            System.out.println("Workspaces root already exists: " + workspacesRootPath);
        }
    }

    @JsonProperty("templates")
    private List<Template> getTemplatesForJson() {
        return new ArrayList<>(templates);
    }

    @JsonProperty("templates")
    private void setTemplatesForJson(List<Template> list) {
        templates.setAll(list);
    }

    @JsonProperty("modules")
    private List<EtmModule> getModulesForJson() {
        return new ArrayList<>(modules);
    }

    @JsonProperty("modules")
    private void setModulesForJson(List<EtmModule> list) {
        modules.setAll(list);
    }

    @JsonProperty("blocks")
    private List<PersonalisationBlock> getBlocksForJson() {
        return new ArrayList<>(blocks);
    }

    @JsonProperty("blocks")
    private void setBlocksForJson(List<PersonalisationBlock> list) {
        blocks.setAll(list);
    }

    @JsonProperty("contexts")
    private List<ContextXml> getContextsForJson() {
        return new ArrayList<>(contexts);
    }

    @JsonProperty("contexts")
    private void setContextsForJson(List<ContextXml> list) {
        contexts.setAll(list);
    }

    public static Path getWorkspacesRootPath() {
        File userDir = new File(System.getProperty("user.home"));
        return userDir.toPath().resolve(workspacesRootName);
    }

    public void createNewWorkspace() {

        Path templateFolder = rootFolderPath.resolve("Templates");
        Path moduleFolder = rootFolderPath.resolve("Modules");
        Path blocksFolder = rootFolderPath.resolve("Blocks");
        Path contextFolder = rootFolderPath.resolve("ContextXml");

        if (Files.exists(configFilePath)
                || Files.exists(templateFolder)
                || Files.exists(moduleFolder)
                || Files.exists(blocksFolder)
                || Files.exists(contextFolder)) {
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

        writeToJson(configFilePath);
    }

    public Path getRootFolderPath() {
        return rootFolderPath;
    }

    public Path getTemplatesPath() {
        return rootFolderPath.resolve("Templates");
    }

    public Path getModulesPath() {
        return rootFolderPath.resolve("Modules");
    }

    public Path getBlocksPath() {
        return rootFolderPath.resolve("Blocks");
    }

    public Path getContextXmlPath() {
        return rootFolderPath.resolve("ContextXml");
    }

    public void openWorkspace(Path workspaceRootPath) {
        readFromJson(workspaceRootPath);
    }

    public void addNewWorkspaceFile(Path filePath, WorkspaceFileType fileType) throws IDEException {

        if (fileExists(filePath, fileType)) {
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

        if (fileExists(filePath, fileType)) {
            return;
        }

        switch (fileType) {
            case TEMPLATE -> templates.add(new Template(filePath));
            case MODULE -> modules.add(new EtmModule(filePath));
            case BLOCK -> blocks.add(new PersonalisationBlock(filePath));
            case CONTEXT -> contexts.add(new ContextXml(filePath));
        }
    }

    public void removeWorkspaceFile(WorkspaceFile fileToRemove, boolean deleteFromFileSystem) {
        System.out.println("Removing: " + fileToRemove.getBaseFileName());
        if (deleteFromFileSystem) {
            System.out.println("Deleting: " + fileToRemove.getFilePath());
            try {
                Files.delete(fileToRemove.getFilePath());
            } catch (IOException ioe) {
                throw new IDEException("An error occurred deleting the file from the file system: " + fileToRemove.getFilePath(), ioe.getCause());
            }
        }

        switch(fileToRemove.getWorkspaceFileType()) {
            case TEMPLATE:
                templates.remove(fileToRemove);
                break;
            case MODULE:
                modules.remove(fileToRemove);
                break;
            case BLOCK:
                blocks.remove(fileToRemove);
                break;
            case CONTEXT:
                contexts.remove(fileToRemove);
                break;
        }
        writeToJson();
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

    public Optional<PersonalisationBlock> getBlockByName(String blockName) {
        blockName += ".block";
        String finalBlockName = blockName;
        return blocks.stream()
                .filter(block -> finalBlockName.equals(block.getFileName().toString()))
                .findFirst();
    }

    private Optional<ContextXml> getContext(String contextFilePath) {
        return contexts.stream()
                .filter(context -> contextFilePath.equals(context.getFilePath().toString()))
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

    /* Observables to support the automatic update of the WorkspaceExplorer */
    public ObservableList<Template> getTemplates() {
        return templates;
    }

    public ObservableList<EtmModule> getModules() {
        return modules;
    }

    public ObservableList<PersonalisationBlock> getBlocks() {
        return blocks;
    }

    public ObservableList<ContextXml> getContexts() {
        return contexts;
    }

    /* JSON methods for load and save */
    public void writeToJson(Path jsonFilePath) {
        try {
            JsonUtil.writeToJson(jsonFilePath, this);
            // LogPanel.appendLog("Saved workspace JSON file: " + jsonFilePath);
        } catch (IOException ioe) {
            throw new IDEException("An error occurred saving the workspace JSON file: " + jsonFilePath, ioe.getCause());
        } catch (Exception e) {
            throw new IDEException("An unknown occurred saving the workspace JSON file: " + jsonFilePath, e);
        }
    }

    public void writeToJson() {
        writeToJson(configFilePath);
    }

    public void readFromJson(Path jsonFilePath) {
        try {
            Workspace newWorkspace = JsonUtil.readFromJson(jsonFilePath, Workspace.class);

            this.rootFolderPath = newWorkspace.rootFolderPath;
            this.configFilePath = newWorkspace.configFilePath;

            this.templates.setAll(newWorkspace.templates);
            this.modules.setAll(newWorkspace.modules);
            this.blocks.setAll(newWorkspace.blocks);
            this.contexts.setAll(newWorkspace.contexts);

        } catch (IOException ioe) {
            throw new IDEException("An error occurred loading the workspace JSON file: " + jsonFilePath, ioe.getCause());
        } catch (Exception e) {
            throw new IDEException("An unknown occurred loading the workspace JSON file: " + jsonFilePath, e);
        }
    }
}
