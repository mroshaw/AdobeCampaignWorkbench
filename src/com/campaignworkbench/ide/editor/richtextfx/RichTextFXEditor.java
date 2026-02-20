package com.campaignworkbench.ide.editor.richtextfx;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.ThemeManager;
import com.campaignworkbench.ide.editor.ICodeEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.flowless.VirtualizedScrollPane;
import javafx.scene.Cursor;

/**
 * Implementation of ICodeEditor using the RichTextFX library
 */
public class RichTextFXEditor implements ICodeEditor, IThemeable {

    private final CodeArea codeArea;
    private final VirtualizedScrollPane<CodeArea> scrollPane;
    private final BorderPane root;

    // Syntax highlighting
    private ISyntaxStyler syntaxStyler;

    // Code Folding
    private IFoldParser foldParser;


    /**
     * Constructor
     */
    public RichTextFXEditor(SyntaxType syntaxType) {
        codeArea = new CodeArea();
        codeArea.setCursor(Cursor.TEXT);

        scrollPane = new VirtualizedScrollPane<>(codeArea);
        root = new BorderPane(scrollPane);
        root.getStyleClass().add("code-editor");

        setSyntaxStyler(syntaxType);
        setFoldingParser(syntaxType);

        // Re-highlight on text change
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, syntaxStyler.style(codeArea.getText()));
        });

        GutterFactory gutterFactory = new GutterFactory(codeArea, foldParser);
        codeArea.setParagraphGraphicFactory(gutterFactory);
        Platform.runLater(() -> ThemeManager.register(this));
    }

    @Override
    public Node getNode() {
        return root;
    }

    @Override
    public void refreshContent() {
        // RichTextFX usually handles its own repainting
    }

    @Override
    public void setText(String text) {
        codeArea.replaceText(text);
        // codeArea.setStyleSpans(0, syntaxStyler.style(text));
    }

    @Override
    public String getText() {
        return codeArea.getText();
    }

    private void setFoldingParser(SyntaxType syntax) {
        // Set folding class instance
        switch (syntax) {
            case XML:
                foldParser = new XmlFoldParser(codeArea);
                break;
            default:
                foldParser = new CampaignFoldParser(codeArea);
                break;
        }
    }

    private void setSyntaxStyler(SyntaxType syntax) {
        String newText = codeArea.getText();

        // Trigger highlighting
        switch (syntax) {
            case XML:
                syntaxStyler = new XmlStyler();
                break;
            default:
                syntaxStyler = new CampaignStyler();
        }
    }

    @Override
    public void setEditable(boolean editable) {
        codeArea.setEditable(editable);
    }

    @Override
    public void requestFocus() {
        codeArea.requestFocus();
    }

    @Override
    public void setCaretAtStart() {
        codeArea.moveTo(0);
        codeArea.requestFollowCaret();
    }

    @Override
    public void gotoLine(int line) {
        if (line <= 0) return;
        int paragraphIndex = Math.min(line - 1, codeArea.getParagraphs().size() - 1);
        codeArea.moveTo(paragraphIndex, 0);
        codeArea.selectLine();
        codeArea.requestFollowCaret();
    }

    @Override
    public void applyTheme(IDETheme theme) {

        if (theme == IDETheme.DARK) {
            if (syntaxStyler != null) {
                String codeEditorCss = theme.getCodeEditorStyleSheet();
                // root.getScene().getStylesheets().add(codeEditorCss);
                String languageCss = syntaxStyler.getStyleSheet(theme);
                // root.getScene().getStylesheets().add(languageCss);
                root.getStylesheets().add(codeEditorCss);
                root.getStylesheets().add(languageCss);
            }
        }
    }

    @Override
    public void openFindDialog(String fileName) {

    }
}
