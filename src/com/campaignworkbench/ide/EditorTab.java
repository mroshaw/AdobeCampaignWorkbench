package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.*;
import com.campaignworkbench.ide.editor.*;
import com.campaignworkbench.ide.editor.richtextfx.RichTextFXEditor;
import com.campaignworkbench.util.UiUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Implements a Tab containing a code editor
 */
public final class EditorTab extends Tab {

    private final VBox container;
    private final HBox toolsContainer;
    private final ToolBar formatToolBar;
    private final ToolBar findReplaceToolBar;
    private final WorkspaceFile workspaceFile;
    private final ICodeEditor editor;

    private final TextField findField;

    /**
     * Constructor
     *
     * @param workspaceFile that the editor is editing
     */
    public EditorTab(WorkspaceFile workspaceFile) {

        this.workspaceFile = workspaceFile;

        SyntaxType syntaxType = determineSyntax(workspaceFile.getFilePath());
        this.editor = new RichTextFXEditor(determineSyntax(workspaceFile.getFilePath()));
        updateTabText();

        Button formatButton = UiUtil.createButton("", "Format code", FontAwesomeIcon.ALIGN_LEFT, Color.GREEN, "12px", true, _ -> formatHandler());
        Button foldAllButton = UiUtil.createButton("", "Fold all", FontAwesomeIcon.INDENT, Color.GREEN, "12px", true, _ -> foldAllHandler());
        Button unfoldAllButton = UiUtil.createButton("", "Unfold all", FontAwesomeIcon.DEDENT, Color.GREEN, "12px", true, _ -> unfoldAllHandler());

        formatToolBar = new ToolBar(formatButton, foldAllButton, unfoldAllButton);

        BorderPane root = new BorderPane();
        root.setCenter(editor.getNode());

        toolsContainer = new HBox();

        Label findLabel = new Label("Find:");
        findField = new TextField();
        Button findButton = UiUtil.createButton("", "Find all", FontAwesomeIcon.ARROW_CIRCLE_RIGHT, Color.GREEN, "12px", true, _ -> findHandler());
        Button clearFindButton = UiUtil.createButton("", "Clear", FontAwesomeIcon.TIMES_CIRCLE, Color.RED, "12px", true, _ -> clearFindHandler());
        findReplaceToolBar = new ToolBar(findLabel, findField, findButton, clearFindButton);
        toolsContainer.getChildren().addAll(formatToolBar, findReplaceToolBar);
        HBox.setHgrow(formatToolBar, Priority.ALWAYS);

        container = new VBox(toolsContainer, editor.getNode());
        VBox.setVgrow(editor.getNode(), Priority.ALWAYS);
        setContent(container);
        editor.setText(workspaceFile.getWorkspaceFileContent());
        editor.setCaretAtStart();

        // Set styles
        container.getStyleClass().add("editor-tab");
        formatToolBar.getStyleClass().add("editor-toolbar");
        findReplaceToolBar.getStyleClass().add("editor-toolbar");
    }

    public void setDataContextFile(Path contextFile) {
        if (workspaceFile instanceof WorkspaceContextFile workspaceContextFile) {
            workspaceContextFile.setDataContextFile(contextFile);
            updateTabText();
        } else {
            throw new IDEException("Attempted to set context on a non-context based EditorTab!", null);
        }
    }

    public void clearDataContextFile() {
        setDataContextFile(null);
    }

    public void setMessageContextFile(Path contextFile) {
        if (workspaceFile instanceof Template template) {
            template.setMessageContextFile(contextFile);
            updateTabText();
        } else {
            throw new IDEException("Attempted to set context on a non-context based EditorTab!", null);
        }
    }

    public void clearMessageContextFile() {
        setMessageContextFile(null);
    }

    private void updateTabText() {
        String tabName = workspaceFile.getFileName().toString();
        setText(tabName);
    }

    /**
     * @return workspace file associated with this editor tab
     */
    public WorkspaceFile getWorkspaceFile() {
        return workspaceFile;
    }

    /**
     * Get the file associated with this editor tab
     *
     * @return the file associated with this editor tab
     */
    public Path getFile() {
        return workspaceFile.getFilePath();
    }

    public boolean isContextSet() {
        if (workspaceFile instanceof WorkspaceContextFile workspaceContextFile) {
            return workspaceContextFile.isDataContextSet();
        } else {
            return false;
        }
    }

    public Path getDataContextFilePath() {
        if (workspaceFile instanceof WorkspaceContextFile workspaceContextFile) {
            return workspaceContextFile.getFilePath();
        } else {
            throw new IDEException("Attempted to get context from a non-context based EditorTab!", null);
        }
    }

    public String getDataContextFileContent() {
        if (workspaceFile instanceof WorkspaceContextFile workspaceContextFile) {
            return workspaceContextFile.getDataContextContent();
        } else {
            throw new IDEException("Attempted to get context from a non-context based EditorTab!", null);
        }
    }

    private void formatHandler() {
        editor.formatCode(2);
    }

    private void foldAllHandler() {
        editor.foldAll();
    }

    private void unfoldAllHandler() {
        editor.unfoldAll();
    }

    private void findHandler() {
        String textToFind = findField.getText();
        if(!Objects.equals(textToFind, "")) {
            editor.find(textToFind);
        }
    }

    private void clearFindHandler() {
        // findField.setText("");
        editor.find("");
    }

    /**
     * Refresh the context of the tab
     */
    public void refreshEditor() {
        editor.refreshContent();
    }

    /**
     * Return the text of this editor tab
     *
     * @return text of the editor tab
     */
    public String getEditorText() {
        return editor.getText();
    }

    /**
     * @return the editor within the editor tab
     */
    public ICodeEditor getEditor() {
        return editor;
    }

    /**
     * Derive the underlying SyntaxType for the editor
     *
     * @param file the path to the file to determine syntax for
     * @return the determined syntax type
     */
    private SyntaxType determineSyntax(Path file) {
        String name = file.getFileName().toString().toLowerCase();

        return switch (name) {
            case String s when s.endsWith(".template") || s.endsWith(".module") -> SyntaxType.CAMPAIGN;
            case String s when s.endsWith(".block") -> SyntaxType.CAMPAIGN;
            case String s when s.endsWith(".xml") -> SyntaxType.XML;
            default -> SyntaxType.PLAIN;
        };
    }

    /**
     * Helper for quick check for template type editor
     *
     * @return boolean true if editor has a template file open
     */
    public boolean isTemplateTab() {
        return workspaceFile.isTemplate();
    }

    public boolean isDataContextApplicable() {
        return workspaceFile.isDataContextApplicable();
    }

    public boolean isMessageContextApplicable() {
        return workspaceFile.isMessageContextApplicable();
    }

    public void toggleFind() {
        findReplaceToolBar.setVisible(!findReplaceToolBar.isVisible());
    }

    public void showFind() {
        findReplaceToolBar.setVisible(true);
    }

    public void hideFind() {
        findReplaceToolBar.setVisible(false);
    }
}
