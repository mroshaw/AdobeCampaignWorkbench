package com.campaignworkbench.ide.workspaceexplorer;

import com.campaignworkbench.ide.IJavaFxNode;
import com.campaignworkbench.ide.IdeException;
import com.campaignworkbench.ide.TextInputBox;
import com.campaignworkbench.ide.YesNoPopupDialog;
import com.campaignworkbench.util.FileUtil;
import com.campaignworkbench.util.UiUtil;
import com.campaignworkbench.workspace.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.glyphfont.FontAwesome;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * User interface control to explore and navigate the files in a workspace
 */
public class WorkspaceExplorer implements IJavaFxNode {

    // Context menu labels
    private static final String createNewButtonText = "Create new";
    private static final String addExistingButtonText = "Add existing";
    private static final String removeButtonText = "Remove";

    // Observables
    private final StringProperty workspaceName = new SimpleStringProperty("No workspace selected");
    private final ObjectProperty<Workspace> workspace = new SimpleObjectProperty<>();

    // Change listeners
    private ListChangeListener<Template> templatesListener;
    private ListChangeListener<EtmModule> modulesListener;
    private ListChangeListener<PersoBlock> blocksListener;
    private ListChangeListener<ContextXml> contextsListener;

    // Root structures of the tree view
    private TreeView<Object> treeView;
    private TreeItem<Object> workspaceRoot;
    private TreeItem<Object> templateRoot;
    private TreeItem<Object> moduleRoot;
    private TreeItem<Object> blockRoot;
    private TreeItem<Object> contextRoot;

    // Event handlers
    private final Consumer<WorkspaceFile> fileOpenHandler;
    private final Consumer<Workspace> workspaceChangedHandler;
    private final Consumer<String> insertIntoCodeHandler;

    // Toolbar buttons
    private Button createNewButton;
    private Button addExistingButton;
    private Button removeButton;
    private Button setDataContextButton;
    private Button clearDataContextButton;
    private Button setMessageContextButton;
    private Button clearMessageContextButton;

    // Main panel
    private VBox workspaceExplorerPanel;

    private WorkspaceFileType selectedFileType;
    private WorkspaceFile selectedFile;
    private WorkspaceFile selectedContextFile;

    /**
     * @param labelText           Label to use for the control in the UI
     * @param fileOpenHandler that handles double clicks of files in the Explorer
     */
    public WorkspaceExplorer(String labelText,
                             Consumer<WorkspaceFile> fileOpenHandler,
                             Consumer<Workspace> workspaceChangedHandler,
                             Consumer<String> insertIntoCodeHandler) {

        this.fileOpenHandler = fileOpenHandler;
        this.workspaceChangedHandler = workspaceChangedHandler;
        this.insertIntoCodeHandler = insertIntoCodeHandler;

        createUi(labelText);
    }

    public Workspace getWorkspace() {
        return workspace.getValue();
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace.setValue(workspace);
    }

    public boolean isWorkspaceOpen() {
        return workspace.getValue() != null;
    }

    private void bindWorkspace() {
        workspace.addListener((obs, oldWorkspace, newWorkspace) -> {

            // Remove old listeners
            if (oldWorkspace != null) {
                if (templatesListener != null)
                    oldWorkspace.getTemplates().removeListener(templatesListener);

                if (modulesListener != null)
                    oldWorkspace.getModules().removeListener(modulesListener);

                if (blocksListener != null)
                    oldWorkspace.getBlocks().removeListener(blocksListener);

                if (contextsListener != null)
                    oldWorkspace.getContexts().removeListener(contextsListener);
            }

            // Clear tree
            templateRoot.getChildren().clear();
            moduleRoot.getChildren().clear();
            blockRoot.getChildren().clear();
            contextRoot.getChildren().clear();
            workspaceName.unbind();

            if (newWorkspace == null) {
                workspaceName.set("No workspace selected");
            } else {
                workspaceName.bind(newWorkspace.getNameProperty());

                // Bind children to workspace.templates
                templatesListener = bindListToTree(newWorkspace.getTemplates(), templateRoot, template ->
                        WorkspaceExplorerItem.createTemplateTreeItem(template, this::deleteExistingFile)
                );

                modulesListener = bindListToTree(newWorkspace.getModules(), moduleRoot, module ->
                        WorkspaceExplorerItem.createModuleTreeItem(module, this::insertIntoCode, this::deleteExistingFile)
                );
                blocksListener = bindListToTree(newWorkspace.getBlocks(), blockRoot, block ->
                        WorkspaceExplorerItem.createBlockTreeItem(block, this::insertIntoCode, this::deleteExistingFile)
                );
                contextsListener = bindListToTree(newWorkspace.getContexts(), contextRoot, context ->
                        WorkspaceExplorerItem.createContextTreeItem(context, this::deleteExistingFile)
                );
            }
        });
    }

    private <T> ListChangeListener<T> bindListToTree(
            ObservableList<T> list,
            TreeItem<Object> parentRoot,
            Function<T, TreeItem<Object>> mapper) {

        // Initial population
        parentRoot.getChildren().clear();
        for (T item : list) {
            parentRoot.getChildren().add(mapper.apply(item));
        }

        ListChangeListener<T> listener = change -> {
            while (change.next()) {

                if (change.wasRemoved()) {
                    for (T removed : change.getRemoved()) {
                        parentRoot.getChildren().removeIf(child ->
                                child.getValue() == removed ||
                                        (child.getValue() instanceof WorkspaceExplorerItem.WorkspaceFileTreeItem wft &&
                                                wft.workspaceFile == removed)
                        );
                    }
                }

                if (change.wasAdded()) {
                    for (T added : change.getAddedSubList()) {
                        parentRoot.getChildren().add(mapper.apply(added));
                    }
                }
            }
        };

        list.addListener(listener);
        return listener;
    }

    private void createUi(String labelText) {
        // Create the UI
        Label explorerLabel = new Label(labelText);
        explorerLabel.getStyleClass().add("ide-label");

        // Mini toolbar
        createNewButton = UiUtil.createButton("", "Create new", FontAwesome.Glyph.FILE, "neutral-icon", 1, true, _ -> createNewHandler());
        addExistingButton = UiUtil.createButton("", "Add existing", FontAwesome.Glyph.PLUS_CIRCLE, "positive-icon", 1, true, _ -> addExistingHandler());
        removeButton = UiUtil.createButton("", "Remove", FontAwesome.Glyph.MINUS_CIRCLE, "negative-icon", 1, true, _ -> deleteHandler());
        setDataContextButton = UiUtil.createButton("", "Set Data Context", FontAwesome.Glyph.FILE_CODE_ALT, "positive-icon", 1, true, _ -> setDataContextHandler());
        clearDataContextButton = UiUtil.createButton("", "Clear Data Context", FontAwesome.Glyph.FILE_CODE_ALT, "negative-icon", 1, true, _ -> clearDataContextHandler());
        setMessageContextButton = UiUtil.createButton("", "Set Message Context", FontAwesome.Glyph.ENVELOPE, "positive-icon", 1, true, _ -> setMessageContextHandler());
        clearMessageContextButton = UiUtil.createButton("", "Clear Message Context", FontAwesome.Glyph.ENVELOPE, "negative-icon", 1, true, _ -> clearMessageContextHandler());
        ToolBar toolbar = new ToolBar(createNewButton, addExistingButton, removeButton, setDataContextButton, clearDataContextButton, setMessageContextButton, clearMessageContextButton);

        // TreeView for all items
        treeView = new TreeView<>();
        treeView.getStyleClass().add("workspace-explorer-treeview");
        // Bind the workspace to the TreeView
        bindWorkspace();

        // Create roots
        workspaceRoot = WorkspaceExplorerItem.createHeaderTreeItemObservableText(FontAwesome.Glyph.FOLDER, workspaceName,
                "workspace-icon", null, this::createNewFile, this::addExistingFile);

        templateRoot = WorkspaceExplorerItem.createHeaderTreeItemStaticText(FontAwesome.Glyph.ENVELOPE, "Templates",
                "template-icon", WorkspaceFileType.TEMPLATE, this::createNewFile, this::addExistingFile);

        moduleRoot = WorkspaceExplorerItem.createHeaderTreeItemStaticText(FontAwesome.Glyph.TASKS, "Modules",
                "module-icon", WorkspaceFileType.MODULE, this::createNewFile, this::addExistingFile);

        blockRoot = WorkspaceExplorerItem.createHeaderTreeItemStaticText(FontAwesome.Glyph.LIST, "Blocks",
                "block-icon", WorkspaceFileType.BLOCK, this::createNewFile, this::addExistingFile);

        contextRoot = WorkspaceExplorerItem.createHeaderTreeItemStaticText(
                FontAwesome.Glyph.FILE_CODE_ALT, "Contexts",
                "context-icon", WorkspaceFileType.CONTEXT, this::createNewFile, this::addExistingFile);

        workspaceRoot.getChildren().setAll(templateRoot, moduleRoot, blockRoot, contextRoot);
        treeView.setRoot(workspaceRoot);
        workspaceRoot.setExpanded(true);

        // Apply the custom cell factory to render the tree nodes
        WorkspaceExplorerItem.applyCellFactory(treeView);

        // Create the main explorer container
        workspaceExplorerPanel = new VBox(explorerLabel, toolbar, treeView);
        workspaceExplorerPanel.setMinHeight(0);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        // Listen for selection changes, so we can add context to the toolbar buttons
        treeView.getSelectionModel().selectedItemProperty().addListener(this::selectionChangedHandler);

        // Listen for double clicks
        setupDoubleClickHandler();

        // Set style classes
        workspaceExplorerPanel.getStyleClass().add("workspace-explorer");
        toolbar.getStyleClass().add("small-toolbar");

        setToolbarContext();
    }

    private void selectionChangedHandler(ObservableValue obs, TreeItem oldItem, TreeItem newItem) {
        if (newItem != null) {

            if (newItem.getValue() instanceof WorkspaceExplorerItem.ContextTreeItem contextTreeItem) {
                selectedContextFile = contextTreeItem.workspaceFile;

                TreeItem parentItem = newItem.getParent();

                if (parentItem != null && parentItem.getValue() instanceof WorkspaceExplorerItem.WorkspaceFileTreeItem parentWorkspaceItem) {
                    selectedFile = parentWorkspaceItem.workspaceFile;
                    selectedFileType = selectedFile.getFileType();
                }

                selectedFileType = selectedFile.getFileType();
            } else if (newItem.getValue() instanceof WorkspaceExplorerItem.WorkspaceFileTreeItem file) {
                selectedFile = file.workspaceFile;
                selectedFileType = selectedFile.getFileType();

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

        Optional<String> result = TextInputBox.show(getWindow(), "Create workspace", "Please enter a unique name for the new workspace", "Workspace name:");

        result.ifPresent(workspaceName -> {
            // Only executed if OK was clicked and text was not empty
            System.out.println("Workspace Name: " + workspaceName);

            setWorkspace(new Workspace(workspaceName, true));
        });
    }

    public void openWorkspace() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Workspace Folder");
        directoryChooser.setInitialDirectory(
                Workspace.getWorkspacesRootPath().toFile()
        );

        File folder = directoryChooser.showDialog(getWindow());

        if (folder == null) return;

        // Construct expected JSON file path
        String folderName = folder.toPath().getFileName().toString();
        Path expectedJsonFile = folder.toPath().resolve(folderName + ".json");

        if (Files.exists(expectedJsonFile) && Files.isRegularFile(expectedJsonFile)) {
            System.out.println("File exists: " + expectedJsonFile);
            setWorkspace(new Workspace(folderName, false));
            getWorkspace().load();
        } else {
            // Not a valid workspace
            throw new IdeException("Selected folder is not a valid workspace!", null);
        }
    }

    public void saveWorkspace() {
        if (getWorkspace() != null) {
            try {
                getWorkspace().save();
            } catch (IdeException ideException) {
                throw new IdeException("Could not save workspace!", ideException.getCause());
            }
        }
    }

    public void closeWorkspace() {

    }

    public void createNewFile(WorkspaceFileType workspaceFileType) {
        File selectedFile = FileUtil.openFile(getWorkspace(), workspaceFileType, "Create new", getWindow());

        if (selectedFile == null) {
            return;
        }

        getWorkspace().createNewWorkspaceFile(selectedFile.getName(), workspaceFileType);
    }

    public void addExistingFile(WorkspaceFileType workspaceFileType) {
        File selectedFile = FileUtil.openFile(getWorkspace(), workspaceFileType, "Select existing", getWindow());

        if (selectedFile == null) {
            return;
        }

        getWorkspace().addWorkspaceFile(selectedFile.getName(), workspaceFileType);
    }

    private void deleteExistingFile(WorkspaceFile workspaceFile) {
        YesNoPopupDialog.YesNoCancel result = YesNoPopupDialog.show("Confirm delete?", "Do you also want to delete the selected file (" + selectedFile.getBaseFileName() + ") from the file system?", (Stage) getNode().getScene().getWindow());

        if (result == YesNoPopupDialog.YesNoCancel.CANCEL) {
            return;
        }
        getWorkspace().removeWorkspaceFile(selectedFile, result == YesNoPopupDialog.YesNoCancel.YES);
    }

    private void insertIntoCode(String textToInsert) {
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

    private void deleteHandler() {
        if (selectedFile == null) {
            return;
        }
        deleteExistingFile(selectedFile);
    }

    private void setDataContextHandler() {

        if (selectedFile instanceof WorkspaceContextFile workspaceContextFile) {

            Optional<ContextXml> contextFile = WorkspaceFilePicker.show(getWindow(), "Pick Context", "Choose an XML context for the message", "Context XML: ", getWorkspace().getContexts());
            contextFile.ifPresent(workspaceContextFile::setDataContextFile);
        }
    }

    private void setMessageContextHandler() {
        if (selectedFile instanceof Template template) {

            Optional<ContextXml> contextFile = WorkspaceFilePicker.show(getWindow(), "Pick Context", "Choose an XML context for the message", "Context XML: ", getWorkspace().getContexts());
            contextFile.ifPresent(template::setMessageContextFile);
        }
    }

    private void clearDataContextHandler() {
        if (selectedFile instanceof WorkspaceContextFile workspaceFile) {
            workspaceFile.clearDataContext();
        }
    }

    private void clearMessageContextHandler() {
        if (selectedFile instanceof Template workspaceFile) {
            workspaceFile.clearMessageContext();
        }
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
                    WorkspaceFile fileToOpen= contextTreeItem.workspaceFile;
                    // fileToOpen.setWorkspace(workspace);

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

    private Window getWindow() {
        return workspaceExplorerPanel.getScene().getWindow();
    }

    @Override
    public Node getNode() {
        return workspaceExplorerPanel;
    }
}
