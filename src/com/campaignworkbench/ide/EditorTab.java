package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.CodeEditor;
import com.campaignworkbench.ide.editor.RSyntaxEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

import java.nio.file.Path;

public final class EditorTab extends Tab {

    private final Path file;
    private final CodeEditor editor;

    public EditorTab(Path file, String initialText) {
        this.file = file;
        this.editor = new RSyntaxEditor();

        setText(file.getFileName().toString());

        BorderPane root = new BorderPane();
        root.setCenter(editor.getNode());
        setContent(root);

        editor.setText(initialText);
        editor.setSyntax(determineSyntax(file));
    }

    public Path getFile() {
        return file;
    }

    public String getEditorText() {
        return editor.getText();
    }

    private SyntaxType determineSyntax(Path file) {
        String name = file.getFileName().toString().toLowerCase();

        if (name.endsWith(".template")) return SyntaxType.TEMPLATE;
        if (name.endsWith(".block"))    return SyntaxType.BLOCK;
        if (name.endsWith(".xml"))      return SyntaxType.XML;

        return SyntaxType.PLAIN;
    }

    public boolean isTemplate() {
        return determineSyntax(file) == SyntaxType.TEMPLATE;
    }
}
