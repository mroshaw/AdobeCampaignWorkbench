package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.*;
import com.campaignworkbench.ide.editor.*;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

import java.nio.file.Path;

/**
 * Implements a Tab containing a code editor
 */
public final class EditorTab extends Tab {

    private final WorkspaceFile workspaceFile;
    private final ICodeEditor editor;

    /**
     * Constructor
     *
     * @param workspaceFile that the editor is editing
     */
    public EditorTab(WorkspaceFile workspaceFile) {

        this.workspaceFile = workspaceFile;

        // this.editor = new RichTextFXEditor();
        this.editor = new RSyntaxEditor();
        updateTabText();

        BorderPane root = new BorderPane();
        root.setCenter(editor.getNode());
        setContent(root);

        editor.setText(workspaceFile.getWorkspaceFileContent());
        editor.setSyntax(determineSyntax(workspaceFile.getFilePath()));
        editor.setCaretAtStart();
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
        /*
        // If context is set, append that
        if (workspaceFile instanceof WorkspaceContextFile workspaceContextFile) {
            if (workspaceContextFile.isDataContextSet()) {
                tabName += " (" + workspaceContextFile.getDataContextFileName().toString() + ")";
            } else {
                tabName += " (NOT CONTEXT SET)";
            }
        }
        */
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
            case String s when s.endsWith(".template") || s.endsWith(".module") -> SyntaxType.TEMPLATE;
            case String s when s.endsWith(".block") -> SyntaxType.BLOCK;
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

    public void openFindDialog() {
        String fileName = workspaceFile!= null? workspaceFile.getBaseFileName() : "New File";
        editor.openFindDialog(fileName);
    }

}
