package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.*;
import com.campaignworkbench.util.FileUtil;
import com.campaignworkbench.util.UiUtil;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.glyphfont.FontAwesome;

import java.io.File;
import java.util.function.Consumer;

/**
 * User interface control to explore and navigate the files in a workspace
 */
public class WorkspaceExplorer implements IJavaFxNode {

    private static final String createNewButtonText = "Create new";
    private static final String addExistingButtonText = "Add existing";
    private static final String removeButtonText = "Remove";

    private Workspace workspace;
    private final TreeView<Object> treeView;
    private final Consumer<WorkspaceFile> fileOpenHandler;
    private final Consumer<Workspace> workspaceChangedHandler;
    private final Consumer<String> insertIntoCodeHandler;

    private TreeItem<Object> templateRoot;
    private TreeItem<Object> moduleRoot;
    private TreeItem<Object> blockRoot;
    private TreeItem<Object> contextRoot;

    private final Button createNewButton;
    private final Button addExistingButton;
    private final Button removeButton;
    private final Button setDataContextButton;
    private final Button clearDataContextButton;
    private final Button setMessageContextButton;
    private final Button clearMessageContextButton;

    /**
     * The panel containing the workspace explorer
     */
    private final VBox workspaceExplorerPanel;

    private WorkspaceFileType selectedFileType;
    private WorkspaceFile selectedFile;
    private WorkspaceFile selectedContextFile;

    /**
     * @param label           Label to use for the control in the UI
     * @param fileOpenHandler that handles double clicks of files in the Explorer
     */
    public WorkspaceExplorer(String label,
                             Workspace workspace,
                             Consumer<WorkspaceFile> fileOpenHandler,
                             Consumer<Workspace> workspaceChangedHandler,
                             Consumer<String> insertIntoCodeHandler) {
        this.workspace = workspace;
        treeView = new TreeView<>();
        setupDoubleClickHandler();

        this.fileOpenHandler = fileOpenHandler;
        this.workspaceChangedHandler = workspaceChangedHandler;
        this.insertIntoCodeHandler = insertIntoCodeHandler;

        Label explorerLabel = new Label(label);
        explorerLabel.setPadding(new Insets(0, 0, 0, 5));
        explorerLabel.getStyleClass().add("ide-label");

        // Create the toolbar
        createNewButton = UiUtil.createButton("", "Create new", FontAwesome.Glyph.FILE, "neutral-icon", 1, true, _ -> createNewHandler());
        addExistingButton = UiUtil.createButton("", "Add existing", FontAwesome.Glyph.PLUS_CIRCLE, "positive-icon", 1, true, _ -> addExistingHandler());
        removeButton = UiUtil.createButton("", "Remove", FontAwesome.Glyph.MINUS_CIRCLE, "negative-icon", 1, true, _ -> removeHandler());
        setDataContextButton = UiUtil.createButton("", "Set Data Context", FontAwesome.Glyph.FILE_CODE_ALT, "positive-icon", 1, true, _ -> setDataContextHandler());
        clearDataContextButton = UiUtil.createButton("", "Clear Data Context", FontAwesome.Glyph.FILE_CODE_ALT, "negative-icon", 1, true, _ -> clearDataContextHandler());
        setMessageContextButton = UiUtil.createButton("", "Set Message Context", FontAwesome.Glyph.ENVELOPE, "positive-icon", 1, true, _ -> setMessageContextHandler());
        clearMessageContextButton = UiUtil.createButton("", "Clear Message Context", FontAwesome.Glyph.ENVELOPE, "negative-icon", 1, true, _ -> clearMessageContextHandler());
        ToolBar toolbar = new ToolBar(createNewButton, addExistingButton, removeButton, setDataContextButton, clearDataContextButton, setMessageContextButton, clearMessageContextButton);

        // Create the main explorer container
        workspaceExplorerPanel = new VBox(explorerLabel, toolbar, treeView);
        workspaceExplorerPanel.setMinHeight(0);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        if (workspace != null) {
            setWorkspace(workspace);
        }

        // Listen for selection changes, so we can add context to the toolbar buttons
        treeView.getSelectionModel().selectedItemProperty().addListener(this::selectionChangedHandler);

        // Set style classes
        workspaceExplorerPanel.getStyleClass().add("workspace-explorer");
        toolbar.getStyleClass().add("small-toolbar");

        setToolbarContext();
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public boolean isWorkspaceOpen() {
        return workspace != null;
    }

    private void selectionChangedHandler(ObservableValue obs, TreeItem oldItem, TreeItem newItem) {
        if (newItem != null) {

            if (newItem.getValue() instanceof WorkspaceExplorerItem.ContextTreeItem contextTreeItem) {
                selectedContextFile = contextTreeItem.workspaceFile;

                TreeItem parentItem = newItem.getParent();

                if (parentItem != null && parentItem.getValue() instanceof WorkspaceExplorerItem.WorkspaceFileTreeItem parentWorkspaceItem) {
                    selectedFile = parentWorkspaceItem.workspaceFile;
                    selectedFileType = selectedFile.getWorkspaceFileType();
                }

                selectedFileType = selectedFile.getWorkspaceFileType();
            } else if (newItem.getValue() instanceof WorkspaceExplorerItem.WorkspaceFileTreeItem file) {
                selectedFile = file.workspaceFile;
                selectedFileType = selectedFile.getWorkspaceFileType();

            } else if (newItem.getValue() instanceof WorkspaceExplorerItem.HeaderTreeItem parentFolder) {
                selectedFileType = parentFolder.fileType;
                selectedFile = null;
                selectedContextFile = null;
            }
            setToolbarContext();
        }
    }

    private void setToolbarContext() {

        if (selectedFile == null) {
            setDataContextButton.setDisable(true);
            clearDataContextButton.setDisable(true);
            setMessageContextButton.setDisable(true);
            clearMessageContextButton.setDisable(true);
        }

        if (selectedFileType == null) {
            addExistingButton.setDisable(true);
            createNewButton.setDisable(true);
            removeButton.setDisable(true);
            return;
        }

        createNewButton.setTooltip(new Tooltip(createNewButtonText + " " + selectedFileType.toString().toLowerCase()));
        addExistingButton.setTooltip(new Tooltip(addExistingButtonText + " " + selectedFileType.toString().toLowerCase()));
        removeButton.setTooltip(new Tooltip(removeButtonText + " " + selectedFileType.toString().toLowerCase()));

        boolean isWorkspaceFileSelected = selectedFile != null;
        boolean isTemplateSelected = selectedFileType == WorkspaceFileType.TEMPLATE && isWorkspaceFileSelected;
        boolean isModuleSelected = selectedFileType == WorkspaceFileType.MODULE && isWorkspaceFileSelected;
        boolean isBlockSelected = selectedFileType == WorkspaceFileType.BLOCK && isWorkspaceFileSelected;

        addExistingButton.setDisable(false);
        createNewButton.setDisable(false);
        removeButton.setDisable(!isWorkspaceFileSelected);

        switch (selectedFileType) {
            case TEMPLATE:
                setDataContextButton.setDisable(!isTemplateSelected);
                clearDataContextButton.setDisable(!isTemplateSelected);
                setMessageContextButton.setDisable(!isTemplateSelected);
                clearMessageContextButton.setDisable(!isTemplateSelected);

                break;
            case MODULE:
                setDataContextButton.setDisable(!isModuleSelected);
                clearDataContextButton.setDisable(!isModuleSelected);
                setMessageContextButton.setDisable(true);
                clearMessageContextButton.setDisable(true);
                break;
            case BLOCK:
            case CONTEXT:
            default:
                setDataContextButton.setDisable(true);
                clearDataContextButton.setDisable(true);
                setMessageContextButton.setDisable(true);
                clearMessageContextButton.setDisable(true);
                break;
        }
    }

    public void createNewWorkspace() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Workspace JSON file");
        fileChooser.setInitialDirectory(
                Workspace.getWorkspacesRootPath().toFile()
        );
        fileChooser.setInitialFileName("workspace.json");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Workbench JSON files", "*.json")
        );

        File selectedFile = fileChooser.showSaveDialog(getWindow());

        if (selectedFile == null) {
            return;
        }

        workspace = new Workspace(selectedFile.toPath(), true);
    }

    public void openWorkspace() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Workspace JSON file");
        fileChooser.setInitialDirectory(
                Workspace.getWorkspacesRootPath().toFile()
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Workbench JSON files", "*.json")
        );

        File selectedFile = fileChooser.showOpenDialog(getWindow());

        if (selectedFile == null) return;

        if (selectedFile.getName().endsWith(".json")) {

            Workspace newWorkspace = new Workspace(selectedFile.toPath(), false);
            newWorkspace.openWorkspace(selectedFile.toPath());
            setWorkspace(newWorkspace);
            return;
        }

        // Not a valid workspace file
        throw new IDEException("Selected file is not a valid workspace JSON file.", null);
    }

    public void closeWorkspace() {
        setWorkspace(null);
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;

        if (this.workspace == null) {
            treeView.setRoot(null);
            return;
        }

        TreeItem<Object> root = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesome.Glyph.FOLDER,
                workspace.getRootFolderPath().getFileName().toString(),
                "16px", 5, "workspace-icon", null, this::createNewFile, this::addExistingFile
        );

        templateRoot = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesome.Glyph.ENVELOPE, "Templates", "16px", 5, "template-icon", WorkspaceFileType.TEMPLATE, this::createNewFile, this::addExistingFile
        );

        moduleRoot = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesome.Glyph.TASKS, "Modules", "16px", 5, "module-icon", WorkspaceFileType.MODULE, this::createNewFile, this::addExistingFile
        );

        blockRoot = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesome.Glyph.LIST, "Blocks", "16px", 5, "block-icon", WorkspaceFileType.BLOCK, this::createNewFile, this::addExistingFile
        );

        contextRoot = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesome.Glyph.FILE_CODE_ALT, "Contexts", "16px", 5, "context-icon", WorkspaceFileType.CONTEXT, this::createNewFile, this::addExistingFile
        );

        root.getChildren().setAll(templateRoot, moduleRoot, blockRoot, contextRoot);
        root.setExpanded(true);

        treeView.setRoot(root);
        WorkspaceExplorerItem.enableMixedContent(treeView, 2, 6);

        bindWorkspace();

        populateInitial();

        workspaceChangedHandler.accept(workspace);
    }

    // Populate current workspace lists into the existing roots (no re-creation)
    private void populateInitial() {
        templateRoot.getChildren().clear();
        for (Template template : workspace.getTemplates()) {
            TreeItem<Object> item = WorkspaceExplorerItem.createWorkspaceFileTreeItem(template, insertIntoCodeHandler, this::deleteWorkspaceFile);
            item.getChildren().add(WorkspaceExplorerItem.createContextFileTreeItem(template.getDataContextFile(), "Data"));
            item.getChildren().add(WorkspaceExplorerItem.createContextFileTreeItem(template.getMessageContextFile(), "Message"));
            item.setExpanded(true);
            templateRoot.getChildren().add(item);
        }
        templateRoot.setExpanded(true);

        moduleRoot.getChildren().clear();
        for (EtmModule module : workspace.getModules()) {
            TreeItem<Object> item = WorkspaceExplorerItem.createWorkspaceFileTreeItem(module, insertIntoCodeHandler, this::deleteWorkspaceFile);
            item.getChildren().add(WorkspaceExplorerItem.createContextFileTreeItem(module.getDataContextFile(), "Data"));
            Platform.runLater(() -> moduleRoot.getChildren().add(item));
        }

        blockRoot.getChildren().clear();
        for (PersonalisationBlock block : workspace.getBlocks()) {
            Platform.runLater(() -> blockRoot.getChildren().add(WorkspaceExplorerItem.createWorkspaceFileTreeItem(block, insertIntoCodeHandler, this::deleteWorkspaceFile)));
        }

        contextRoot.getChildren().clear();
        for (ContextXml context : workspace.getContexts()) {
            Platform.runLater(() -> contextRoot.getChildren().add(WorkspaceExplorerItem.createWorkspaceFileTreeItem(context, insertIntoCodeHandler, this::deleteWorkspaceFile)));
        }
    }

    public void createNewFile(WorkspaceFileType workspaceFileType) {
        FileChooser fileChooser = new FileChooser();

        FileChooserConfig chooserConfig = getFileChooserConfig(workspaceFileType, "Create new");

        fileChooser.setTitle(chooserConfig.title());
        fileChooser.setInitialDirectory(chooserConfig.defaultFolder());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(chooserConfig.description(), chooserConfig.extension())
        );

        File selectedFile = fileChooser.showSaveDialog(this.getWindow());

        if (selectedFile == null) {
            return;
        }

        workspace.addNewWorkspaceFile(selectedFile.toPath(), workspaceFileType);
    }

    public void addExistingFile(WorkspaceFileType workspaceFileType) {
        FileChooser fileChooser = new FileChooser();
        FileChooserConfig chooserConfig = getFileChooserConfig(workspaceFileType, "Add existing");

        fileChooser.setTitle(chooserConfig.title());
        fileChooser.setInitialDirectory(chooserConfig.defaultFolder());

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(chooserConfig.description(), chooserConfig.extension())
        );

        File selectedFile = fileChooser.showOpenDialog(this.getWindow());
        if (selectedFile == null) {
            return;
        }

        workspace.addExistingWorkspaceFile(selectedFile.toPath(), workspaceFileType);
    }

    public void saveWorkspace() {
        if (workspace != null) {
            try {
                workspace.writeToJson();
            } catch (IDEException ideException) {
                throw new IDEException("Could not save workspace!", ideException.getCause());
            }
        }
    }

    public void insertIntoCode(String textToInsert) {
        insertIntoCodeHandler.accept(textToInsert);
    }

    private void createNewHandler() {

        if (selectedFileType == null) {
            return;
        }

        createNewFile(selectedFileType);
    }

    private void addExistingHandler() {
        if (selectedFileType == null) {
            return;
        }

        addExistingFile(selectedFileType);
    }

    private void deleteWorkspaceFile(WorkspaceFile workspaceFile) {
        YesNoPopupDialog.YesNoCancel result = YesNoPopupDialog.show("Confirm delete?", "Do you also want to delete the selected file (" + selectedFile.getBaseFileName() + ") from the file system?", (Stage) getNode().getScene().getWindow());

        if (result == YesNoPopupDialog.YesNoCancel.CANCEL) {
            return;
        }
        workspace.removeWorkspaceFile(selectedFile, result == YesNoPopupDialog.YesNoCancel.YES);
    }

    private void removeHandler() {
        if (selectedFile == null) {
            return;
        }
        deleteWorkspaceFile(selectedFile);
    }

    private void setDataContextHandler() {
        File contextFile = FileUtil.openFile(workspace, WorkspaceFileType.CONTEXT, getWindow());
        if (contextFile != null) {
            if (selectedFile instanceof WorkspaceContextFile workspaceFile) {
                workspaceFile.setDataContextFile(contextFile.toPath());
                refreshContextChild(selectedFile, "Data");
                workspace.writeToJson();
            }
        }
    }

    private void setMessageContextHandler() {
        File contextFile = FileUtil.openFile(workspace, WorkspaceFileType.CONTEXT, getWindow());
        if (contextFile != null) {
            if (selectedFile instanceof Template workspaceFile) {
                workspaceFile.setMessageContextFile(contextFile.toPath());
                refreshContextChild(selectedFile, "Message");
                workspace.writeToJson();
            }
        }
    }

    private void clearDataContextHandler() {
        if (selectedFile instanceof WorkspaceContextFile workspaceFile) {
            workspaceFile.clearDataContext();
            refreshContextChild(selectedFile, "Data");
            workspace.writeToJson();
        }
    }

    private void clearMessageContextHandler() {
        if (selectedFile instanceof Template workspaceFile) {
            workspaceFile.clearMessageContext();
            refreshContextChild(selectedFile, "Message");
            workspace.writeToJson();
        }
    }

    private Window getWindow() {
        return workspaceExplorerPanel.getScene().getWindow();
    }

    private void setupDoubleClickHandler() {
        treeView.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, evt -> {
            if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {

                TreeItem<Object> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem == null) return;

                Object selectedObject = selectedItem.getValue();

                // Action depends on the type of the underlying item double-clicked

                // Folder header item
                if (selectedObject instanceof WorkspaceExplorerItem.HeaderTreeItem) {
                    // Allow the double click to fold/unfolder
                    return;
                }

                // Context child item
                if (selectedObject instanceof WorkspaceExplorerItem.ContextTreeItem contextTreeItem) {
                    if (contextTreeItem.workspaceFile == null) {
                        // Choose the context
                        if (contextTreeItem.contextLabel.startsWith("Data")) {
                            setDataContextHandler();
                        } else {
                            setMessageContextHandler();
                        }
                        return;
                    }

                    // Call open file on the selected context file
                    fileOpenHandler.accept(contextTreeItem.workspaceFile);
                    evt.consume();
                    return;
                }

                // File item
                if (selectedObject instanceof WorkspaceExplorerItem.WorkspaceFileTreeItem workspaceFileTreeItem) {
                    fileOpenHandler.accept(workspaceFileTreeItem.workspaceFile);
                    evt.consume();
                }
            }
        });
    }

    private FileChooserConfig getFileChooserConfig(WorkspaceFileType fileType, String action) {
        return switch (fileType) {
            case TEMPLATE -> new FileChooserConfig(
                    action + " template file",
                    workspace.getTemplatesPath().toFile(),
                    "Template files",
                    "*.template"
            );
            case MODULE -> new FileChooserConfig(
                    action + " module file",
                    workspace.getModulesPath().toFile(),
                    "ETM Module files",
                    "*.module"
            );
            case BLOCK -> new FileChooserConfig(
                    action + " block file",
                    workspace.getBlocksPath().toFile(),
                    "Block files",
                    "*.block"
            );
            case CONTEXT -> new FileChooserConfig(
                    action + " context file",
                    workspace.getContextXmlPath().toFile(),
                    "Context XML files",
                    "*.xml"
            );
        };
    }

    private void refreshContextChild(WorkspaceFile file, String label) {
        TreeItem<Object> parent = findTreeItemForFile(file);
        if (parent == null) return;


        // Record current selection
        int selectedIndex = treeView.getSelectionModel().getSelectedIndex();

        // Find existing child by label
        for (int i = 0; i < parent.getChildren().size(); i++) {
            TreeItem<Object> child = parent.getChildren().get(i);

            Object val = child.getValue();
            if (val instanceof WorkspaceExplorerItem.ContextTreeItem ctxItem &&
                    ctxItem.contextLabel.equals(label)) {

                // Replace child TreeItem
                parent.getChildren().set(
                        i,
                        WorkspaceExplorerItem.createContextFileTreeItem(
                                file instanceof Template t && label.equals("Message") ? t.getMessageContextFile() :
                                        file instanceof WorkspaceContextFile c && label.equals("Data") ? c.getDataContextFile() :
                                                null,
                                label
                        )
                );

                // Restore selection
                treeView.getSelectionModel().select(selectedIndex);

                return;
            }
        }
    }

    private TreeItem<Object> findTreeItemForFile(WorkspaceFile file) {
        // Search the whole tree recursively
        return findTreeItemRecursive(treeView.getRoot(), file);
    }

    private TreeItem<Object> findTreeItemRecursive(TreeItem<Object> root, WorkspaceFile file) {
        for (TreeItem<Object> child : root.getChildren()) {
            Object val = child.getValue();
            if (val instanceof WorkspaceExplorerItem.WorkspaceFileTreeItem wf &&
                    wf.workspaceFile == file) {
                return child;
            }
            TreeItem<Object> match = findTreeItemRecursive(child, file);
            if (match != null) return match;
        }
        return null;
    }


    private void bindWorkspace() {

        workspace.getTemplates().addListener((ListChangeListener<Template>) changedTemplate -> {
            while (changedTemplate.next()) {
                if (changedTemplate.wasAdded()) {
                    for (Template template : changedTemplate.getAddedSubList()) {
                        TreeItem<Object> item = WorkspaceExplorerItem.createWorkspaceFileTreeItem(template, insertIntoCodeHandler, this::deleteWorkspaceFile);
                        item.getChildren().add(
                                WorkspaceExplorerItem.createContextFileTreeItem(template.getDataContextFile(), "Data")
                        );
                        item.getChildren().add(
                                WorkspaceExplorerItem.createContextFileTreeItem(template.getMessageContextFile(), "Message")
                        );
                        Platform.runLater(() -> templateRoot.getChildren().add(item));
                    }
                }
                if (changedTemplate.wasRemoved()) {

                    Platform.runLater(() ->templateRoot.getChildren().removeIf(ti ->
                            changedTemplate.getRemoved().contains(((WorkspaceExplorerItem.WorkspaceFileTreeItem) ti.getValue()).workspaceFile)
                    ));

                }
            }
        });

        workspace.getModules().addListener((ListChangeListener<EtmModule>) changedModule -> {
            while (changedModule.next()) {
                if (changedModule.wasAdded()) {
                    for (EtmModule module : changedModule.getAddedSubList()) {
                        TreeItem<Object> item = WorkspaceExplorerItem.createWorkspaceFileTreeItem(module, insertIntoCodeHandler, this::deleteWorkspaceFile);
                        item.getChildren().add(
                                WorkspaceExplorerItem.createContextFileTreeItem(module.getDataContextFile(), "Data")
                        );
                        Platform.runLater(() -> moduleRoot.getChildren().add(item));
                    }
                }
                if (changedModule.wasRemoved()) {
                    Platform.runLater(() ->moduleRoot.getChildren().removeIf(
                            ti -> changedModule.getRemoved().contains(
                                    ((WorkspaceExplorerItem.WorkspaceFileTreeItem) ti.getValue()).workspaceFile
                            )
                    ));
                }
            }
        });

        workspace.getBlocks().addListener((ListChangeListener<PersonalisationBlock>) changedBlock -> {
            while (changedBlock.next()) {
                if (changedBlock.wasAdded()) {
                    for (PersonalisationBlock block : changedBlock.getAddedSubList()) {
                        Platform.runLater(() -> blockRoot.getChildren().add(
                                WorkspaceExplorerItem.createWorkspaceFileTreeItem(block, insertIntoCodeHandler, this::deleteWorkspaceFile)
                        ));
                    }
                }
                if (changedBlock.wasRemoved()) {
                    Platform.runLater(() ->blockRoot.getChildren().removeIf(
                            ti -> changedBlock.getRemoved().contains(
                                    ((WorkspaceExplorerItem.WorkspaceFileTreeItem) ti.getValue()).workspaceFile
                            )
                    ));
                }
            }
        });

        workspace.getContexts().addListener((ListChangeListener<ContextXml>) changedContext -> {
            while (changedContext.next()) {
                if (changedContext.wasAdded()) {
                    for (ContextXml ctx : changedContext.getAddedSubList()) {
                        Platform.runLater(() -> contextRoot.getChildren().add(
                                WorkspaceExplorerItem.createWorkspaceFileTreeItem(ctx, insertIntoCodeHandler, this::deleteWorkspaceFile)
                        ));
                    }
                }
                if (changedContext.wasRemoved()) {
                    Platform.runLater(() -> contextRoot.getChildren().removeIf(
                            ti -> changedContext.getRemoved().contains(
                                    ((WorkspaceExplorerItem.WorkspaceFileTreeItem) ti.getValue()).workspaceFile
                            )
                    ));
                }
            }
        });
    }

    @Override
    public Node getNode() {
        return workspaceExplorerPanel;
    }
}
