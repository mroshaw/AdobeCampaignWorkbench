package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.*;
import com.campaignworkbench.util.FileUtil;
import com.campaignworkbench.util.UiUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

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

    private TreeItem<Object> root;
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
    public WorkspaceExplorer(String label, Workspace workspace, Consumer<WorkspaceFile> fileOpenHandler) {
        this.workspace = workspace;
        treeView = new TreeView<>();
        setupDoubleClickHandler();

        this.fileOpenHandler = fileOpenHandler;

        Label explorerLabel = new Label(label);
        explorerLabel.setPadding(new Insets(0, 0, 0, 5));
        // explorerLabel.setStyle("-fx-font-weight: bold;");

        // Create the toolbar
        HBox toolbar = new HBox();

        createNewButton = UiUtil.createButton("", "Create new", FontAwesomeIcon.FILE, Color.YELLOW, "16px", true, _ -> createNewHandler());
        addExistingButton = UiUtil.createButton("", "Add existing", FontAwesomeIcon.PLUS_CIRCLE, Color.YELLOW, "16px", true, _ -> addExistingHandler());
        removeButton = UiUtil.createButton("", "Remove", FontAwesomeIcon.MINUS_CIRCLE, Color.RED, "16px", true, evt -> removeHandler());
        setDataContextButton = UiUtil.createButton("", "Set Data Context", FontAwesomeIcon.FILE_CODE_ALT, Color.GREEN, "16px", true, _ -> setDataContextHandler());
        clearDataContextButton = UiUtil.createButton("", "Clear Data Context", FontAwesomeIcon.FILE_CODE_ALT, Color.RED, "16px", true, _ -> clearDataContextHandler());
        setMessageContextButton = UiUtil.createButton("", "Set Message Context", FontAwesomeIcon.ENVELOPE, Color.GREEN, "16px", true, _ -> setMessageContextHandler());
        clearMessageContextButton = UiUtil.createButton("", "Clear Message Context", FontAwesomeIcon.ENVELOPE, Color.RED, "16px", true, _ -> clearMessageContextHandler());

        toolbar.getChildren().addAll(createNewButton, addExistingButton, removeButton, setDataContextButton, clearDataContextButton, setMessageContextButton, clearMessageContextButton);

        workspaceExplorerPanel = new VBox(5, explorerLabel, toolbar, treeView);
        workspaceExplorerPanel.setMinHeight(0);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        workspaceExplorerPanel.setPadding(new Insets(0, 0, 0, 5));

        if (workspace != null) {
            setWorkspace(workspace);
        }

        // Listen for selection changes, so we can add context to the toolbar buttons
        treeView.getSelectionModel().selectedItemProperty().addListener(this::selectionChangedHandler);

        setToolbarContext();
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

            // Debug
            System.out.println("Selected Folder Type: " + selectedFileType);
            System.out.println("Workspace File: " + (selectedFile == null ? "None" : selectedFile.getBaseFileName()));
            System.out.println("Selected Context File: " + (selectedContextFile == null ? "None" : selectedContextFile.getBaseFileName()));

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
        Boolean inFolder = selectedFile == null;

        Boolean isWorkspaceFileSelected = selectedFile != null;
        Boolean isTemplateSelected = selectedFileType == WorkspaceFileType.TEMPLATE && isWorkspaceFileSelected;
        Boolean isModuleSelected = selectedFileType == WorkspaceFileType.MODULE && isWorkspaceFileSelected;
        Boolean isBlockSelected = selectedFileType == WorkspaceFileType.BLOCK && isWorkspaceFileSelected;

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

    /**
     * @return the TreeView associated with the Explorer
     */
    public TreeView<Object> getTreeView() {
        return treeView;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;

        root = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesomeIcon.FOLDER,
                workspace.getRootFolderPath().getFileName().toString(),
                "16px", 5, Color.YELLOW, null
        );

        templateRoot = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesomeIcon.ENVELOPE, "Templates", "16px", 5, Color.FORESTGREEN, WorkspaceFileType.TEMPLATE
        );

        moduleRoot = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesomeIcon.TASKS, "Modules", "16px", 5, Color.CORNFLOWERBLUE, WorkspaceFileType.MODULE
        );

        blockRoot = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesomeIcon.LIST, "Blocks", "16px", 5, Color.PALEVIOLETRED, WorkspaceFileType.BLOCK
        );

        contextRoot = WorkspaceExplorerItem.createHeaderTreeItem(
                FontAwesomeIcon.FILE_CODE_ALT, "Contexts", "16px", 5, Color.LIGHTCORAL, WorkspaceFileType.CONTEXT
        );

        root.getChildren().setAll(templateRoot, moduleRoot, blockRoot, contextRoot);
        root.setExpanded(true);

        treeView.setRoot(root);
        WorkspaceExplorerItem.enableMixedContent(treeView, 2, 6);

        // IMPORTANT: bind AFTER roots exist and are set on the TreeView
        bindWorkspace();

        // Populate initial content WITHOUT rebuilding roots:
        populateInitial();
    }

    // Populate current workspace lists into the existing roots (no re-creation)
    private void populateInitial() {
        templateRoot.getChildren().clear();
        for (Template t : workspace.getTemplates()) {
            TreeItem<Object> item = WorkspaceExplorerItem.createWorkspaceFileTreeItem(t);
            item.getChildren().add(WorkspaceExplorerItem.createContextFileTreeItem(t.getDataContextFile(), "Data"));
            item.getChildren().add(WorkspaceExplorerItem.createContextFileTreeItem(t.getMessageContextFile(), "Message"));
            item.setExpanded(true);
            templateRoot.getChildren().add(item);
        }
        templateRoot.setExpanded(true);

        moduleRoot.getChildren().clear();
        for (EtmModule m : workspace.getModules()) {
            TreeItem<Object> item = WorkspaceExplorerItem.createWorkspaceFileTreeItem(m);
            item.getChildren().add(WorkspaceExplorerItem.createContextFileTreeItem(m.getDataContextFile(), "Data"));
            moduleRoot.getChildren().add(item);
        }

        blockRoot.getChildren().clear();
        for (PersonalisationBlock b : workspace.getBlocks()) {
            blockRoot.getChildren().add(WorkspaceExplorerItem.createWorkspaceFileTreeItem(b));
        }

        contextRoot.getChildren().clear();
        for (ContextXml cx : workspace.getContexts()) {
            contextRoot.getChildren().add(WorkspaceExplorerItem.createWorkspaceFileTreeItem(cx));
        }
    }

    public void createNewFile(WorkspaceFileType workspaceFileType) {
        FileChooser fileChooser = new FileChooser();

        FileChooserConfig chooserConfig = getFileChooserConfig(workspaceFileType);

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
        FileChooserConfig chooserConfig = getFileChooserConfig(workspaceFileType);

        fileChooser.setTitle(chooserConfig.title());
        fileChooser.setInitialDirectory(chooserConfig.defaultFolder());
        ;
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(chooserConfig.description(), chooserConfig.extension())
        );

        File selectedFile = fileChooser.showOpenDialog(this.getWindow());
        if (selectedFile == null) {
            return;
        }

        workspace.addExistingWorkspaceFile(selectedFile.toPath(), workspaceFileType);
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

    private void removeHandler() {
        if (selectedFile == null) {
            return;
        }
        YesNoPopupDialog.YesNoCancel result = YesNoPopupDialog.show("Confirm Delete?", "Do you also want to delete the selected file (" + selectedFile.getBaseFileName() + ") from the file system?", (Stage) getNode().getScene().getWindow());

        if (result == YesNoPopupDialog.YesNoCancel.CANCEL) {
            return;
        }

        workspace.removeWorkspaceFile(selectedFile, result == YesNoPopupDialog.YesNoCancel.YES);
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

                // Action depends on the type of underlying item double clicked

                // Folder header item
                if (selectedObject instanceof WorkspaceExplorerItem.HeaderTreeItem parentFolder) {
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
                    return;
                }
            }
        });
    }

    private FileChooserConfig getFileChooserConfig(WorkspaceFileType fileType) {
        return switch (fileType) {
            case TEMPLATE -> new FileChooserConfig(
                    "Create new template file",
                    workspace.getTemplatesPath().toFile(),
                    "Template files",
                    "*.template"
            );
            case MODULE -> new FileChooserConfig(
                    "Create new module file",
                    workspace.getModulesPath().toFile(),
                    "ETM Module files",
                    "*.module"
            );
            case BLOCK -> new FileChooserConfig(
                    "Create new block file",
                    workspace.getBlocksPath().toFile(),
                    "Block files",
                    "*.block"
            );
            case CONTEXT -> new FileChooserConfig(
                    "Create new context file",
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

        workspace.getTemplates().addListener((ListChangeListener<Template>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Template t : c.getAddedSubList()) {
                        TreeItem<Object> item = WorkspaceExplorerItem.createWorkspaceFileTreeItem(t);
                        item.getChildren().add(
                                WorkspaceExplorerItem.createContextFileTreeItem(t.getDataContextFile(), "Data")
                        );
                        item.getChildren().add(
                                WorkspaceExplorerItem.createContextFileTreeItem(t.getMessageContextFile(), "Message")
                        );
                        templateRoot.getChildren().add(item);
                    }
                }
                if (c.wasRemoved()) {

                    templateRoot.getChildren().removeIf(ti ->
                            c.getRemoved().contains(((WorkspaceExplorerItem.WorkspaceFileTreeItem) ti.getValue()).workspaceFile)
                    );

                }
            }
        });

        workspace.getModules().addListener((ListChangeListener<EtmModule>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (EtmModule m : c.getAddedSubList()) {
                        TreeItem<Object> item = WorkspaceExplorerItem.createWorkspaceFileTreeItem(m);
                        item.getChildren().add(
                                WorkspaceExplorerItem.createContextFileTreeItem(m.getDataContextFile(), "Data")
                        );
                        moduleRoot.getChildren().add(item);
                    }
                }
                if (c.wasRemoved()) {
                    moduleRoot.getChildren().removeIf(
                            ti -> c.getRemoved().contains(
                                    ((WorkspaceExplorerItem.WorkspaceFileTreeItem) ti.getValue()).workspaceFile
                            )
                    );
                }
            }
        });

        workspace.getBlocks().addListener((ListChangeListener<PersonalisationBlock>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (PersonalisationBlock b : c.getAddedSubList()) {
                        blockRoot.getChildren().add(
                                WorkspaceExplorerItem.createWorkspaceFileTreeItem(b)
                        );
                    }
                }
                if (c.wasRemoved()) {
                    blockRoot.getChildren().removeIf(
                            ti -> c.getRemoved().contains(
                                    ((WorkspaceExplorerItem.WorkspaceFileTreeItem) ti.getValue()).workspaceFile
                            )
                    );
                }
            }
        });

        workspace.getContexts().addListener((ListChangeListener<ContextXml>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (ContextXml ctx : c.getAddedSubList()) {
                        contextRoot.getChildren().add(
                                WorkspaceExplorerItem.createWorkspaceFileTreeItem(ctx)
                        );
                    }
                }
                if (c.wasRemoved()) {
                    contextRoot.getChildren().removeIf(
                            ti -> c.getRemoved().contains(
                                    ((WorkspaceExplorerItem.WorkspaceFileTreeItem) ti.getValue()).workspaceFile
                            )
                    );
                }
            }
        });
    }

    @Override
    public Node getNode() {
        return workspaceExplorerPanel;
    }
}
