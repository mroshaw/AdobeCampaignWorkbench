package com.campaignworkbench.ide.editor.richtextfx;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.fxmisc.richtext.CodeArea;

import java.util.function.IntFunction;

/**
 * A JavaFX node factory that creates a gutter for each paragraph in the CodeArea.
 */
public class GutterFactory implements IntFunction<Node> {

    private final CodeArea codeArea;
    private final IFoldParser foldParser;
    private FoldRegions foldRegions;

    // private final Text iconRight = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ARROW_CIRCLE_RIGHT, "12px");
    // private final Text iconDown = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ARROW_CIRCLE_DOWN, "12px");

    public GutterFactory(CodeArea codeArea, IFoldParser foldParser) {
        this.codeArea = codeArea;
        this.foldParser = foldParser;

        codeArea.setParagraphGraphicFactory(this);
    }

    @Override
    public Node apply(int paragraphIndex) {

        // Refresh the folding state
        foldRegions = foldParser.findFoldRegions(codeArea);

        if(foldRegions.isParagraphHidden(paragraphIndex)) {
            return null;
        }

        // Create an HBox to contain the line number and fold indicator
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(60);
        box.getStyleClass().add("gutter");
        // Get the line number node as a node
        Label lineNo = (Label) SimpleLineNumberFactory.get(codeArea).apply(paragraphIndex);
        lineNo.setMinWidth(36);
        lineNo.getStyleClass().add("line-number");
        // Create a fold indicator label
        Label foldIndicator = new Label();
        // foldIndicator.setMinWidth(20);
        foldIndicator.getStyleClass().add("custom-fold-indicator");

        setFoldIndicator(foldIndicator, paragraphIndex);

        // Add the line number and fold indicator to the container and return
        box.getChildren().addAll(lineNo, foldIndicator);
        return box;
    }

    private void setFoldIndicator(Label foldIndicator, int paragraphIndex) {
        if (foldParser.isParagraphFolded(paragraphIndex)) {

            // foldIndicator.setGraphic(iconRight);
            foldIndicator.setText("▶");
            foldIndicator.setCursor(Cursor.HAND);
            foldIndicator.setOnMouseClicked(e -> {
                e.consume();
                foldParser.unfoldParagraph(paragraphIndex);

                // foldParser.removeFoldedParagraph(paragraphIndex);
                // codeArea.unfoldParagraphs(paragraphIndex);

                // Refresh
                codeArea.setParagraphGraphicFactory(codeArea.getParagraphGraphicFactory());
            });
        } else if (foldRegions.isParagraphFoldable(paragraphIndex)) {
            // foldIndicator.setGraphic(iconDown);
            foldIndicator.setText("▼");
            foldIndicator.setCursor(Cursor.HAND);
            foldIndicator.setOnMouseClicked(e -> {
                e.consume();
                foldParser.foldParagraph(paragraphIndex);

                // foldParser.addFoldedParagraph(paragraphIndex);
                // int endParagraphIndex = foldRegions.getFoldedParagraphEnd(paragraphIndex);
                // codeArea.foldParagraphs(paragraphIndex, endParagraphIndex);

                // Refresh
                codeArea.setParagraphGraphicFactory(codeArea.getParagraphGraphicFactory());
            });
        } else {
            foldIndicator.setText("");
            foldIndicator.setOnMouseClicked(null);
        }
    }
}
