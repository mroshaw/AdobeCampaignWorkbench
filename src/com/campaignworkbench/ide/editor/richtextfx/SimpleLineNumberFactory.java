package com.campaignworkbench.ide.editor.richtextfx;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.fxmisc.richtext.GenericStyledArea;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.util.function.IntFunction;

public class SimpleLineNumberFactory<PS> implements IntFunction<Node> {

    private static final Insets DEFAULT_INSETS =
            new Insets(0.0, 5.0, 0.0, 5.0);

    private static final Paint DEFAULT_TEXT_FILL =
            Color.web("#666");

    private static final Font DEFAULT_FONT =
            Font.font("monospace", FontPosture.ITALIC, 13.0);

    private static final Background DEFAULT_BACKGROUND =
            new Background(
                    new BackgroundFill(
                            Color.web("#ddd"),
                            (CornerRadii) null,
                            (Insets) null
                    )
            );

    private final Val<Integer> nParagraphs;
    private final IntFunction<String> format;

    /* =======================
       Static factory methods
       ======================= */

    public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area) {
        return get(area, digits -> "%1$" + digits + "s");
    }

    public static <PS> IntFunction<Node> get(
            GenericStyledArea<PS, ?, ?> area,
            IntFunction<String> format
    ) {
        return new SimpleLineNumberFactory<>(area, format);
    }

    /* =======================
       Constructor
       ======================= */

    private SimpleLineNumberFactory(
            GenericStyledArea<PS, ?, ?> area,
            IntFunction<String> format
    ) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
    }

    /* =======================
       Core implementation
       ======================= */

    @Override
    public Node apply(int idx) {

        Val<String> formatted = nParagraphs.map(n -> format(idx + 1, n));

        Label lineNo = new Label();
        lineNo.setFont(DEFAULT_FONT);
        lineNo.setBackground(DEFAULT_BACKGROUND);
        lineNo.setTextFill(DEFAULT_TEXT_FILL);
        lineNo.setPadding(DEFAULT_INSETS);
        lineNo.setAlignment(Pos.TOP_RIGHT);
        lineNo.getStyleClass().add("line-number");

        lineNo.textProperty().bind(
                formatted.conditionOnShowing(lineNo)
        );

        return lineNo;
    }

    private String format(int x, int max) {
        int digits = (int) Math.floor(Math.log10(max)) + 1;
        return String.format(format.apply(digits), x);
    }
}
