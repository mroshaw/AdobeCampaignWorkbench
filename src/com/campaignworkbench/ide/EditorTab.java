package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.ICodeEditor;
import com.campaignworkbench.ide.editor.RSyntaxEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

import java.nio.file.Path;

/**
 * Implements a Tab containing a code editor
 */
public final class EditorTab extends Tab {

    private final Path file;
    private final ICodeEditor editor;

    /**
     * Constructor
     * @param file that the editor is editing
     * @param initialText any default, initial code to include in the new editor
     */
    public EditorTab(Path file, String initialText) {
        this.file = file;
        this.editor = new RSyntaxEditor();

        setText(file.getFileName().toString());

        BorderPane root = new BorderPane();
        root.setCenter(editor.getNode());
        setContent(root);

        editor.setText(initialText);
        editor.setSyntax(determineSyntax(file));
        editor.setCaretAtStart();
    }

    /**
     * Get the file associated with this editor tab
     * @return the file associated with this editor tab
     */
    public Path getFile() {
        return file;
    }


    /**
     * Refresh the context of the tab
     */
    public void refreshEditor() {
        editor.refreshContent();
    }

    /**
     * Return the text of this editor tab
     * @return text of the editor tab
     */
    public String getEditorText() {
        return editor.getText();
    }

    /**
     * @return the editor within the editor tab
     */
    public ICodeEditor getEditor() { return editor ; }

    /**
     * Derive the underlying SyntaxType for the editor
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
     * @return boolean true if editor has a template file open
     */
    public boolean isTemplate() {
        return determineSyntax(file) == SyntaxType.TEMPLATE;
    }
}
