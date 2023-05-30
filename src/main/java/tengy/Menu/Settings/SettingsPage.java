package tengy.Menu.Settings;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import tengy.Menu.MenuController;
import tengy.Menu.MenuState;
import tengy.PlaybackSettings.PlaybackSettingsState;
import tengy.SVG;
import tengy.Subtitles.SubtitlesState;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class SettingsPage {

    MenuController menuController;

    StackPane settingsWrapper = new StackPane();
    VBox settingsContainer = new VBox();
    VBox settingsBar = new VBox();
    public ScrollPane settingsScroll = new ScrollPane();
    VBox settingsContent = new VBox();

    StackPane titleContainer = new StackPane();
    Button linksButton = new Button();
    Region linksIcon = new Region();
    SVGPath linksSVG = new SVGPath();

    public Label settingsTitle = new Label("Settings");

    HBox buttonBar = new HBox();
    Button subtitlesButton = new Button("Subtitles");
    Button metadataButton = new Button("Metadata editing");
    Button preferencesButton = new Button("Preferences");
    Button musicLibrariesButton = new Button("Music libraries");
    Button controlsButton = new Button("Controls");
    Button aboutButton = new Button("About");


    public SubtitleSection subtitleSection;
    MetadataSection metadataSection;
    public PreferencesSection preferencesSection;
    LibrariesSection librariesSection;
    public ControlsSection controlsSection;
    AboutSection aboutSection;

    public SettingsMenu settingsMenu;

    IntegerProperty focus = new SimpleIntegerProperty(-1);
    List<Node> focusNodes = new ArrayList<>();

    public SettingsPage(MenuController menuController){

        this.menuController = menuController;

        subtitleSection = new SubtitleSection(this);
        metadataSection = new MetadataSection(this);
        preferencesSection = new PreferencesSection(this);
        librariesSection = new LibrariesSection(this);
        controlsSection = new ControlsSection(this);
        aboutSection = new AboutSection(this);

        settingsContainer.setBackground(Background.EMPTY);

        settingsBar.setFillWidth(true);

        settingsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        settingsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        settingsScroll.getStyleClass().add("menuScroll");
        settingsScroll.setFitToWidth(true);
        settingsScroll.setFitToHeight(true);
        settingsScroll.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        settingsScroll.setBackground(Background.EMPTY);

        settingsContent.setBackground(Background.EMPTY);
        settingsContent.setPadding(new Insets(0, 50,20, 50));

        VBox.setVgrow(settingsScroll, Priority.ALWAYS);

        titleContainer.getChildren().addAll(settingsTitle, linksButton);
        titleContainer.setPadding(new Insets(0, 30, 0, 0));

        StackPane.setAlignment(settingsTitle, Pos.CENTER_LEFT);
        settingsTitle.getStyleClass().add("menuTitle");

        StackPane.setAlignment(linksButton, Pos.CENTER_RIGHT);
        linksSVG.setContent(SVG.MENU.getContent());

        linksIcon.setShape(linksSVG);
        linksIcon.getStyleClass().add("graphic");
        linksIcon.setPrefSize(20, 20);
        linksIcon.setMaxSize(20, 20);
        linksIcon.setMouseTransparent(true);

        linksButton.setGraphic(linksIcon);
        linksButton.setCursor(Cursor.HAND);
        linksButton.getStyleClass().add("transparentButton");
        linksButton.visibleProperty().bind(settingsBar.widthProperty().lessThanOrEqualTo(800));
        linksButton.setOnAction(e -> {

            if(menuController.subtitlesController.subtitlesState != SubtitlesState.CLOSED) menuController.subtitlesController.closeSubtitles();
            if(menuController.playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) menuController.playbackSettingsController.closeSettings();

            if(settingsMenu.showing) settingsMenu.hide();
            else settingsMenu.showOptions(true);
        });

        settingsBar.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            if(oldValue.doubleValue() <= 800 && newValue.doubleValue() > 800){
                if(settingsMenu.showing) settingsMenu.hide();
            }
        });

        settingsBar.setPadding(new Insets(55, 50, 20, 50));
        settingsBar.setSpacing(10);
        settingsBar.setAlignment(Pos.CENTER_LEFT);
        settingsBar.getChildren().addAll(titleContainer, buttonBar);


        buttonBar.setTranslateX(-11);
        buttonBar.setSpacing(15);
        buttonBar.getChildren().addAll(subtitlesButton, metadataButton, preferencesButton, musicLibrariesButton, controlsButton, aboutButton);
        buttonBar.visibleProperty().bind(settingsBar.widthProperty().greaterThan(800));

        subtitlesButton.getStyleClass().addAll("linkButton", "settingsBarButton");
        subtitlesButton.setOnAction(e -> {
            if(menuController.subtitlesController.subtitlesState != SubtitlesState.CLOSED) menuController.subtitlesController.closeSubtitles();
            if(menuController.playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) menuController.playbackSettingsController.closeSettings();

            animateScroll(Section.SUBTITLES);

        });
        subtitlesButton.setFocusTraversable(false);

        metadataButton.getStyleClass().addAll("linkButton", "settingsBarButton");
        metadataButton.setOnAction(e -> {
            if(menuController.subtitlesController.subtitlesState != SubtitlesState.CLOSED) menuController.subtitlesController.closeSubtitles();
            if(menuController.playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) menuController.playbackSettingsController.closeSettings();

            animateScroll(Section.METADATA);

        });
        metadataButton.setFocusTraversable(false);


        preferencesButton.getStyleClass().addAll("linkButton", "settingsBarButton");
        preferencesButton.setOnAction(e -> {
            if(menuController.subtitlesController.subtitlesState != SubtitlesState.CLOSED) menuController.subtitlesController.closeSubtitles();
            if(menuController.playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) menuController.playbackSettingsController.closeSettings();

            animateScroll(Section.PREFERENCES);
        });
        preferencesButton.setFocusTraversable(false);

        musicLibrariesButton.getStyleClass().addAll("linkButton", "settingsBarButton");
        musicLibrariesButton.setOnAction(e -> {
            if(menuController.subtitlesController.subtitlesState != SubtitlesState.CLOSED) menuController.subtitlesController.closeSubtitles();
            if(menuController.playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) menuController.playbackSettingsController.closeSettings();

            animateScroll(Section.LIBRARIES);
        });
        musicLibrariesButton.setFocusTraversable(false);


        controlsButton.getStyleClass().addAll("linkButton", "settingsBarButton");
        controlsButton.setOnAction(e -> {
            if(menuController.subtitlesController.subtitlesState != SubtitlesState.CLOSED) menuController.subtitlesController.closeSubtitles();
            if(menuController.playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) menuController.playbackSettingsController.closeSettings();

            animateScroll(Section.CONTROLS);

        });
        controlsButton.setFocusTraversable(false);


        aboutButton.getStyleClass().addAll("linkButton", "settingsBarButton");
        aboutButton.setOnAction(e -> {
            if(menuController.subtitlesController.subtitlesState != SubtitlesState.CLOSED) menuController.subtitlesController.closeSubtitles();
            if(menuController.playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) menuController.playbackSettingsController.closeSettings();

            animateScroll(Section.ABOUT);
        });
        aboutButton.setFocusTraversable(false);


        settingsScroll.setContent(settingsContent);
        settingsScroll.addEventFilter(KeyEvent.ANY, e -> {
            if(e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN){
                e.consume();
            }
        });


        settingsContainer.getChildren().addAll(settingsBar, settingsScroll);
        settingsWrapper.getChildren().add(settingsContainer);
        menuController.settingsContainer.getChildren().add(settingsWrapper);


        settingsContent.getChildren().addAll(subtitleSection, metadataSection, preferencesSection, librariesSection, controlsSection, aboutSection);
        settingsContent.setSpacing(30);


        Platform.runLater(() -> settingsMenu = new SettingsMenu(this));
    }


    public void openSettingsPage(){
        menuController.settingsContainer.setVisible(true);
    }

    public void closeSettingsPage(){
        menuController.settingsContainer.setVisible(false);
    }

    public void enter(){

        if(menuController.menuInTransition) return;

        menuController.menuBar.setActiveButton(menuController.menuBar.settingsButton);
        settingsScroll.setVvalue(0);

        if(menuController.menuState == MenuState.CLOSED){
            if(!menuController.extended.get()) menuController.setMenuExtended(MenuState.SETTINGS_OPEN);
            menuController.openMenu(MenuState.SETTINGS_OPEN);
        }
        else {
            if(!menuController.extended.get()) menuController.extendMenu(MenuState.SETTINGS_OPEN);
            else menuController.animateStateSwitch(MenuState.SETTINGS_OPEN);
        }
    }

    public void animateScroll(Section section){
        double scrollTo = getTargetScrollValue(section);

        Timeline scrollTimeline = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(settingsScroll.vvalueProperty(), scrollTo, Interpolator.EASE_BOTH)));

        scrollTimeline.playFromStart();
    }

    public double getTargetScrollValue(Section section){

        switch(section){
            case SUBTITLES -> {
                return 0;
            }
            case METADATA -> {
                return calculateVvalue(metadataSection);
            }
            case PREFERENCES -> {
                return calculateVvalue(preferencesSection);
            }
            case LIBRARIES -> {
                return calculateVvalue(librariesSection);
            }
            case CONTROLS -> {
                return calculateVvalue(controlsSection);
            }
            case ABOUT -> {
                return calculateVvalue(aboutSection);
            }
            default -> {
                return 1;
            }
        }
    }

    private double calculateVvalue(Node node){
            double heightViewPort = settingsScroll.getViewportBounds().getHeight();
            double heightScrollPane = settingsScroll.getContent().getBoundsInLocal().getHeight();
            double y = node.getBoundsInParent().getMinY();

            return (y/(heightScrollPane-heightViewPort));

    }

    public void loadPreferences(){
        subtitleSection.loadPreferences();
        preferencesSection.loadPreferences();
    }

    public void focusForward() {
    }

    public void focusBackward() {
    }
}
