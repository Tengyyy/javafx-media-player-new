package hans.Menu;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ExpandableTextArea extends TextArea {

    public ExpandableTextArea() {
        this.getStyleClass().add("expandable-text-area");

        setPrefHeight(36);
        setMinHeight(36);
        setMaxHeight(36);

        disableKeys();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();


        setWrapText(true);
        setPadding(new Insets(0, 0, 0, 0));

        ScrollPane scrollPane = (ScrollPane)lookup(".scroll-pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPadding(new Insets(0, 0, 0, 0));

        StackPane viewport = (StackPane) scrollPane.lookup(".viewport");
        viewport.setPadding(new Insets(0, 0, 0, 0));

        Region content = (Region) viewport.lookup(".content");
        content.setPadding(new Insets(7, 10, 7, 10));

        Text text = (Text) content.lookup(".text");

        text.layoutBoundsProperty().addListener((observableBoundsAfter, boundsBefore, boundsAfter) -> {
            if(boundsBefore.getHeight() != boundsAfter.getHeight()){
                double textHeight = boundsAfter.getHeight();
                setMinHeight(textHeight + 16);
                setPrefHeight(textHeight + 16);
                setMaxHeight(textHeight + 16);

                Platform.runLater(this::requestLayout);
            }
        });

    }

    private void disableKeys() {
        setOnKeyPressed(event -> {
            if (event.getCode() != KeyCode.ESCAPE) {
                event.consume();
            }
        });
    }
}