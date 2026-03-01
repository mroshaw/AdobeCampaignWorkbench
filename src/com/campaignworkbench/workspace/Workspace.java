package com.campaignworkbench.workspace;

import com.campaignworkbench.ide.IdeException;
import com.campaignworkbench.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    private static final Path workspacesRootPath = Paths.get(System.getProperty("user.home")).resolve(workspacesRootName);

    // Observable lists to allow WorkspaceExplorer to auto update
    @JsonIgnore
    private final ObservableList<Template> templates =
            FXCollections.observableArrayList();

    @JsonIgnore
    private final ObservableList<EtmModule> modules =
            FXCollections.observableArrayList();

    @JsonIgnore
    private final ObservableList<PersoBlock> blocks =
            FXCollections.observableArrayList();

    @JsonIgnore
    private final ObservableList<ContextXml> contexts =
            FXCollections.observableArrayList();

    @JsonIgnore
    private final StringProperty nameProperty = new SimpleStringProperty();

    // JSON visible properties
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
    private List<PersoBlock> getBlocksForJson() {
        return new ArrayList<>(blocks);
    }

    @JsonProperty("blocks")
    private void setBlocksForJson(List<PersoBlock> list) {
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

    public Workspace(String name, boolean createNew) {

        this.nameProperty.setValue(name);

        if (createNew) {
            createNewWorkspace();
        } else {
            load();
        }
    }

    public Workspace() {
    }

    // Observable properties
    public ObservableList<Template> getTemplates() {
        return templates;
    }
    public ObservableList<EtmModule> getModules() {
        return modules;
    }
    public ObservableList<PersoBlock> getBlocks() {
        return blocks;
    }
    public ObservableList<ContextXml> getContexts() {
        return contexts;
    }
    public StringProperty getNameProperty() { return nameProperty; }
    public String getName() { return nameProperty.getValue(); }
    public Path getRootFolderPath() {
        return workspacesRootPath.resolve(nameProperty.getValue());
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
                throw new IdeException("An error occurred creating Workspaces root: " + workspacesRootPath, ioe.getCause());
            }
        } else {
            System.out.println("Workspaces root already exists: " + workspacesRootPath);
        }
    }

    public static Path getWorkspacesRootPath() {
        File userDir = new File(System.getProperty("user.home"));
        return userDir.toPath().resolve(workspacesRootName);
    }

    public void createNewWorkspace() {
        Path workspaceFolder = getRootFolderPath();
        Path templateFolder = getRootFolderPath().resolve(WorkspaceFileType.TEMPLATE.getFolderName());
        Path moduleFolder = getRootFolderPath().resolve(WorkspaceFileType.MODULE.getFolderName());
        Path blocksFolder = getRootFolderPath().resolve(WorkspaceFileType.BLOCK.getFolderName());
        Path contextFolder = getRootFolderPath().resolve(WorkspaceFileType.CONTEXT.getFolderName());

        if (Files.exists(templateFolder)
                || Files.exists(moduleFolder)
                || Files.exists(blocksFolder)
                || Files.exists(contextFolder)) {
            throw new IdeException("Invalid location selected for new workspace. Workspace files already exists!", null);
        }

        try {
            Files.createDirectory(workspaceFolder);
            Files.createDirectory(templateFolder);
            Files.createDirectory(moduleFolder);
            Files.createDirectory(blocksFolder);
            Files.createDirectory(contextFolder);
        } catch (IOException ioe) {
            throw new IdeException("An error occurred creating the new workspace: " + getConfigFileAbsolutePath(), ioe.getCause());
        }

        save();
    }

    public void load() {
        readFromJson();
    }

    public void save() {
        writeToJson();
    }

    public WorkspaceFile getWorkspaceFile(String fileName, WorkspaceFileType fileType) {

        if (fileName == null) {
            return null;
        }

        ObservableList<? extends WorkspaceFile> fileList = switch(fileType) {
            case TEMPLATE -> templates;
            case MODULE -> modules;
            case BLOCK -> blocks;
            case CONTEXT -> contexts;
        };

        return fileList.stream()
                .filter(t -> fileName.equals(t.getBaseFileName()))
                .findFirst()
                .orElse(null);
    }

    public void createNewWorkspaceFile(String fileName, WorkspaceFileType fileType) throws IdeException {

        if (fileExistsInFileSystem(fileName, fileType)) {
            throw new IdeException("An error occurred creating the new workspace file. File '" + fileName + "' of type '" + fileType + "' already exists!", null);
        }
        Path filePath = getFilePath(fileName, fileType);
        try {
            Path newFilePath = Files.createFile(filePath);
        } catch (IOException ioe) {
            throw new IdeException("An error occurred creating the new workspace file: " + filePath, ioe.getCause());
        }
        addWorkspaceFile(fileName, fileType);
        save();
    }

    public void addWorkspaceFile(String fileName, WorkspaceFileType fileType) {

        if(fileExistsInWorkspace(fileName, fileType)) {
            return;
        }
        switch (fileType) {
            case TEMPLATE -> templates.add(new Template(fileName, this));
            case MODULE -> modules.add(new EtmModule(fileName, this));
            case BLOCK -> blocks.add(new PersoBlock(fileName, this));
            case CONTEXT -> contexts.add(new ContextXml(fileName, this));
        }
        save();
    }

    public void removeWorkspaceFile(WorkspaceFile fileToRemove, boolean deleteFromFileSystem) {
        System.out.println("Removing: " + fileToRemove.getBaseFileName());
        if (deleteFromFileSystem) {
            fileToRemove.deleteFromFileSystem();
        }

        switch (fileToRemove.getFileType()) {
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
        save();
    }


    private String getConfigFileName() {
        return getName() + ".json";
    }

    private Path getConfigFileAbsolutePath() {
        return getRootFolderPath().resolve(getConfigFileName());
    }

    private boolean fileExistsInFileSystem(String fileName, WorkspaceFileType fileType) {
        Path pathToFile = getFilePath(fileName, fileType);
        File file = new File(pathToFile.toString());
        return file.exists();
    }

    private boolean fileExistsInWorkspace(String fileName, WorkspaceFileType fileType) {
        return switch (fileType) {
            case TEMPLATE -> templates.stream().anyMatch(template -> template.getFileName().equals(fileName));
            case MODULE -> modules.stream().anyMatch(module -> module.getFileName().equals(fileName));
            case BLOCK -> blocks.stream().anyMatch(block -> block.getFileName().equals(fileName));
            case CONTEXT -> contexts.stream().anyMatch(context -> context.getFileName().equals(fileName));
            default -> false;
        };
    }

    private Path getFilePath(String fileName, WorkspaceFileType fileType) {
        return getRootFolderPath().resolve(fileType.getFolderName()).resolve(fileName);
    }

    // JSON methods for load and save
    public void writeToJson(Path jsonFilePath) {
        try {
            JsonUtil.writeToJson(jsonFilePath, this);
            System.out.println("Saved workspace JSON file: " + jsonFilePath);
        } catch (IOException ioe) {
            throw new IdeException("An error occurred saving the workspace JSON file: " + jsonFilePath, ioe.getCause());
        } catch (Exception e) {
            throw new IdeException("An unknown occurred saving the workspace JSON file: " + jsonFilePath, e);
        }
    }

    private void writeToJson() {
        writeToJson(getConfigFileAbsolutePath());
    }

    private void readFromJson() {
        try {
            Path jsonFilePath = getConfigFileAbsolutePath();
            Workspace newWorkspace = JsonUtil.readFromJson(jsonFilePath, Workspace.class);

            this.templates.setAll(newWorkspace.templates);
            this.modules.setAll(newWorkspace.modules);
            this.blocks.setAll(newWorkspace.blocks);
            this.contexts.setAll(newWorkspace.contexts);

            // Restore back-references
            this.templates.forEach(template -> {
                template.setWorkspace(this);
                if(template.getMessageContextFile() != null) {
                    template.getMessageContextFile().setWorkspace(this);
                }
                if(template.getDataContextFile() != null) {
                    template.getDataContextFile().setWorkspace(this);
                }
            });
            this.modules.forEach(module -> {
                module.setWorkspace(this);
                if(module.getDataContextFile() != null) {
                    module.getDataContextFile().setWorkspace(this);
                }
            });
            this.blocks.forEach(block -> block.setWorkspace(this));
            this.contexts.forEach(context -> context.setWorkspace(this));

            nameProperty.setValue(jsonFilePath.getParent().getFileName().toString());

        } catch (IOException ioe) {
            throw new IdeException("An error occurred loading the workspace JSON file: " + getConfigFileAbsolutePath(), ioe.getCause());
        } catch (Exception e) {
            throw new IdeException("An unknown occurred loading the workspace JSON file: " + getConfigFileAbsolutePath(), e);
        }
    }
}
