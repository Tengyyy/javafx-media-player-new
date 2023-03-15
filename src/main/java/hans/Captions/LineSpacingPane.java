package hans.Captions;

import hans.*;
import hans.Settings.CheckTab;
import hans.Settings.SettingsController;
import hans.Settings.SettingsState;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.ArrayList;

public class LineSpacingPane {

    CaptionsController captionsController;
    CaptionsOptionsPane captionsOptionsPane;

    ScrollPane scrollPane = new ScrollPane();

    VBox lineSpacingBox = new VBox();
    HBox lineSpacingTitle = new HBox();

    StackPane lineSpacingBackPane = new StackPane();
    Region lineSpacingBackIcon = new Region();
    SVGPath backSVG = new SVGPath();

    Label lineSpacingTitleLabel = new Label();

    CheckTab _0Tab, _50Tab, _75Tab, _100Tab, _125Tab, _150Tab, _200Tab;

    ArrayList<CheckTab> checkTabs = new ArrayList<>();

    LineSpacingPane(CaptionsController captionsController, CaptionsOptionsPane captionsOptionsPane){
        this.captionsController = captionsController;
        this.captionsOptionsPane = captionsOptionsPane;

        backSVG.setContent(App.svgMap.get(SVG.CHEVRON_LEFT));

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("settingsScroll");
        scrollPane.setPrefSize(200, 314);
        scrollPane.setMaxSize(200, 314);
        scrollPane.setContent(lineSpacingBox);
        scrollPane.setVisible(false);
        scrollPane.setMouseTransparent(true);
        scrollPane.setFitToWidth(true);

        StackPane.setAlignment(scrollPane, Pos.BOTTOM_RIGHT);
        lineSpacingBox.setAlignment(Pos.BOTTOM_CENTER);


        lineSpacingBox.setPrefSize(200, 311);
        lineSpacingBox.setMaxSize(200, 311);
        lineSpacingBox.setPadding(new Insets(0, 0, 8, 0));
        lineSpacingBox.setFillWidth(true);
        lineSpacingBox.getChildren().add(lineSpacingTitle);

        lineSpacingTitle.setPrefSize(200, 48);
        lineSpacingTitle.setMaxSize(200, 48);
        lineSpacingTitle.setPadding(new Insets(0, 10, 0, 10));
        VBox.setMargin(lineSpacingTitle, new Insets(0, 0, 10, 0));
        lineSpacingTitle.setAlignment(Pos.CENTER_LEFT);

        lineSpacingTitle.getStyleClass().add("settingsPaneTitle");
        lineSpacingTitle.getChildren().addAll(lineSpacingBackPane, lineSpacingTitleLabel);

        lineSpacingBackPane.setMinSize(24, 40);
        lineSpacingBackPane.setPrefSize(24, 40);
        lineSpacingBackPane.setMaxSize(24, 40);
        lineSpacingBackPane.getChildren().add(lineSpacingBackIcon);
        lineSpacingBackPane.setCursor(Cursor.HAND);

        lineSpacingBackPane.setOnMouseClicked((e) -> closeLineSpacingPane());

        lineSpacingBackIcon.setMinSize(8, 13);
        lineSpacingBackIcon.setPrefSize(8, 13);
        lineSpacingBackIcon.setMaxSize(8, 13);
        lineSpacingBackIcon.getStyleClass().add("settingsPaneIcon");
        lineSpacingBackIcon.setShape(backSVG);

        lineSpacingTitleLabel.setMinHeight(40);
        lineSpacingTitleLabel.setPrefHeight(40);
        lineSpacingTitleLabel.setMaxHeight(40);
        lineSpacingTitleLabel.setText("Line spacing");
        lineSpacingTitleLabel.setCursor(Cursor.HAND);
        lineSpacingTitleLabel.getStyleClass().add("settingsPaneText");
        lineSpacingTitleLabel.setOnMouseClicked((e) -> closeLineSpacingPane());

        _0Tab = new CheckTab(false, "0%");
        _50Tab = new CheckTab(false, "50%");
        _75Tab = new CheckTab(false, "75%");
        _100Tab = new CheckTab(true, "100%");
        _125Tab = new CheckTab(false, "125%");
        _150Tab = new CheckTab(false, "150%");
        _200Tab = new CheckTab(false, "200%");

        lineSpacingBox.getChildren().addAll(_0Tab, _50Tab, _75Tab, _100Tab, _125Tab, _150Tab, _200Tab);
        checkTabs.add(_0Tab);
        checkTabs.add(_50Tab);
        checkTabs.add(_75Tab);
        checkTabs.add(_100Tab);
        checkTabs.add(_125Tab);
        checkTabs.add(_150Tab);
        checkTabs.add(_200Tab);

        _0Tab.setOnMouseClicked(e -> {

            for(CheckTab checkTab : checkTabs){
                checkTab.checkIcon.setVisible(false);
            }

            _0Tab.checkIcon.setVisible(true);
            captionsOptionsPane.lineSpacingTab.subText.setText("0%");

            captionsController.captionsBox.currentSpacing.set(0);

            captionsController.captionsBox.showCaptions();
        });

        _50Tab.setOnMouseClicked(e -> {

            for(CheckTab checkTab : checkTabs){
                checkTab.checkIcon.setVisible(false);
            }

            _50Tab.checkIcon.setVisible(true);
            captionsOptionsPane.lineSpacingTab.subText.setText("50%");

            captionsController.captionsBox.currentSpacing.set((int) (captionsController.captionsBox.defaultSpacing * 0.5));

            captionsController.captionsBox.showCaptions();
        });

        _75Tab.setOnMouseClicked(e -> {

            for(CheckTab checkTab : checkTabs){
                checkTab.checkIcon.setVisible(false);
            }

            _75Tab.checkIcon.setVisible(true);
            captionsOptionsPane.lineSpacingTab.subText.setText("75%");

            captionsController.captionsBox.currentSpacing.set((int) (captionsController.captionsBox.defaultSpacing * 0.75));

            captionsController.captionsBox.showCaptions();
        });

        _100Tab.setOnMouseClicked(e -> {

            for(CheckTab checkTab : checkTabs){
                checkTab.checkIcon.setVisible(false);
            }

            _100Tab.checkIcon.setVisible(true);
            captionsOptionsPane.lineSpacingTab.subText.setText("100%");

            captionsController.captionsBox.currentSpacing.set((int) (captionsController.captionsBox.defaultSpacing));

            captionsController.captionsBox.showCaptions();
        });

        _125Tab.setOnMouseClicked(e -> {

            for(CheckTab checkTab : checkTabs){
                checkTab.checkIcon.setVisible(false);
            }

            _125Tab.checkIcon.setVisible(true);
            captionsOptionsPane.lineSpacingTab.subText.setText("125%");

            captionsController.captionsBox.currentSpacing.set((int) (captionsController.captionsBox.defaultSpacing * 1.25));

            captionsController.captionsBox.showCaptions();
        });

        _150Tab.setOnMouseClicked(e -> {

            for(CheckTab checkTab : checkTabs){
                checkTab.checkIcon.setVisible(false);
            }

            _150Tab.checkIcon.setVisible(true);
            captionsOptionsPane.lineSpacingTab.subText.setText("150%");

            captionsController.captionsBox.currentSpacing.set((int) (captionsController.captionsBox.defaultSpacing * 1.5));

            captionsController.captionsBox.showCaptions();
        });

        _200Tab.setOnMouseClicked(e -> {

            for(CheckTab checkTab : checkTabs){
                checkTab.checkIcon.setVisible(false);
            }

            _200Tab.checkIcon.setVisible(true);
            captionsOptionsPane.lineSpacingTab.subText.setText("200%");

            captionsController.captionsBox.currentSpacing.set((int) (captionsController.captionsBox.defaultSpacing * 2.0));

            captionsController.captionsBox.showCaptions();
        });

        captionsController.captionsPane.getChildren().add(scrollPane);
    }


    public void closeLineSpacingPane(){
        if(captionsController.animating.get()) return;

        captionsController.captionsState = CaptionsState.CAPTIONS_OPTIONS_OPEN;

        captionsController.captionsOptionsPane.scrollPane.setVisible(true);
        captionsController.captionsOptionsPane.scrollPane.setMouseTransparent(false);


        Timeline clipHeightTimeline = new Timeline();
        clipHeightTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(SettingsController.ANIMATION_SPEED), new KeyValue(captionsController.clip.heightProperty(), captionsController.captionsOptionsPane.scrollPane.getHeight())));


        Timeline clipWidthTimeline = new Timeline();
        clipWidthTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(SettingsController.ANIMATION_SPEED), new KeyValue(captionsController.clip.widthProperty(), captionsController.captionsOptionsPane.scrollPane.getWidth())));



        TranslateTransition fontColorTransition = new TranslateTransition(Duration.millis(SettingsController.ANIMATION_SPEED), scrollPane);
        fontColorTransition.setFromX(0);
        fontColorTransition.setToX(scrollPane.getWidth());

        TranslateTransition captionsOptionsTransition = new TranslateTransition(Duration.millis(SettingsController.ANIMATION_SPEED), captionsController.captionsOptionsPane.scrollPane);
        captionsOptionsTransition.setFromX(-scrollPane.getWidth());
        captionsOptionsTransition.setToX(0);


        ParallelTransition parallelTransition = new ParallelTransition(clipHeightTimeline, clipWidthTimeline, fontColorTransition, captionsOptionsTransition);
        parallelTransition.setInterpolator(Interpolator.EASE_BOTH);
        parallelTransition.setOnFinished((e) -> {
            captionsController.animating.set(false);
            scrollPane.setVisible(false);
            scrollPane.setMouseTransparent(true);
            scrollPane.setTranslateX(0);
            captionsController.clip.setHeight(captionsController.captionsOptionsPane.scrollPane.getPrefHeight());
        });

        parallelTransition.play();
        captionsController.animating.set(true);
    }
}




