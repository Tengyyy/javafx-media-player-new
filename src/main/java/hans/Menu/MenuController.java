package hans.Menu;


import hans.*;
import hans.Subtitles.SubtitlesController;
import hans.Subtitles.SubtitlesState;
import hans.Chapters.ChapterController;
import hans.MediaItems.MediaItem;
import hans.Menu.MetadataEdit.MetadataEditPage;
import hans.Menu.Queue.QueuePage;
import hans.Menu.Settings.SettingsPage;
import hans.PlaybackSettings.PlaybackSettingsController;
import hans.PlaybackSettings.PlaybackSettingsState;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;




public class MenuController implements Initializable {


    @FXML
    public
    StackPane menu, menuWrapper, menuContent, sideBar, dragPane, queueContainer, settingsContainer, recentMediaContainer, musicLibraryContainer, playlistsContainer;

    @FXML
    public ScrollPane metadataEditScroll, technicalDetailsScroll, chapterScroll;

    public MainController mainController;
    public ControlBarController controlBarController;
    public PlaybackSettingsController playbackSettingsController;
    public SubtitlesController subtitlesController;
    public MediaInterface mediaInterface;

    public ChapterController chapterController;

    public MetadataEditPage metadataEditPage;
    public TechnicalDetailsPage technicalDetailsPage;
    public QueuePage queuePage;
    public SettingsPage settingsPage;
    public RecentMediaPage recentMediaPage;
    public MusicLibraryPage musicLibraryPage;
    public PlaylistsPage playlistsPage;

    public MenuState menuState = MenuState.CLOSED;

    public boolean menuInTransition = false;

    final public double MIN_WIDTH = 500;

    DragResizer dragResizer;

    public ArrayList<MediaItem> ongoingMetadataEditProcesses = new ArrayList<>();

    public MenuBar menuBar;

    public BooleanProperty extended = new SimpleBooleanProperty(false);

    double shrinkedWidth = MIN_WIDTH;

    SVGPath collapseSVG = new SVGPath();
    SVGPath extendSVG = new SVGPath();
    Region extendIcon = new Region();
    public Button extendButton = new Button();
    ControlTooltip extendTooltip;

    SVGPath closeSVG = new SVGPath();
    Region closeIcon = new Region();
    public Button closeButton = new Button();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        queuePage = new QueuePage(this);
        settingsPage = new SettingsPage(this);
        recentMediaPage = new RecentMediaPage(this);
        musicLibraryPage = new MusicLibraryPage(this);
        playlistsPage = new PlaylistsPage(this);
        metadataEditPage = new MetadataEditPage(this);
        technicalDetailsPage = new TechnicalDetailsPage(this);
        menuBar = new MenuBar(this, sideBar);


        menu.setBackground(Background.EMPTY);

        menu.setPrefWidth(500);
        menu.setMaxWidth(500);
        menu.setViewOrder(1);
        menu.setId("menu");

        menu.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            if(!extended.get()) return;

            if(oldValue.doubleValue() < 1200 && newValue.doubleValue() >= 1200) menuBar.extend();
            else if(oldValue.doubleValue() >= 1200 && newValue.doubleValue() < 1200) menuBar.shrink();
        });

        menu.setTranslateX(-500);

        menu.setMouseTransparent(true);
        Rectangle menuClip = new Rectangle();
        menuClip.widthProperty().bind(menu.widthProperty());
        menuClip.heightProperty().bind(menu.heightProperty());
        menu.setClip(menuClip);

        menu.setOnMouseClicked(e -> {
            if(subtitlesController.subtitlesState != SubtitlesState.CLOSED) subtitlesController.closeSubtitles();
            if(playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) playbackSettingsController.closeSettings();

            menu.requestFocus();
        });


        closeSVG.setContent(App.svgMap.get(SVG.CLOSE));
        closeIcon.setShape(closeSVG);
        closeIcon.setPrefSize(20, 20);
        closeIcon.setMaxSize(20, 20);
        closeIcon.getStyleClass().add("graphic");

        closeButton.setPrefSize(40, 40);
        closeButton.setMaxSize(40, 40);
        closeButton.setCursor(Cursor.HAND);
        closeButton.getStyleClass().add("transparentButton");
        closeButton.setGraphic(closeIcon);
        closeButton.setOnAction(e -> {
            if(subtitlesController.subtitlesState != SubtitlesState.CLOSED) subtitlesController.closeSubtitles();
            if(playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) playbackSettingsController.closeSettings();

            closeMenu();
        });

        closeButton.visibleProperty().bind(extended);
        closeButton.mouseTransparentProperty().bind(extended.not());

        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(10, 10 , 0, 0));

        collapseSVG.setContent(App.svgMap.get(SVG.CHEVRON_LEFT));
        extendSVG.setContent(App.svgMap.get(SVG.CHEVRON_RIGHT));
        extendIcon.setShape(extendSVG);
        extendIcon.setPrefSize(14, 20);
        extendIcon.setMaxSize(14, 20);
        extendIcon.getStyleClass().add("graphic");

        extendButton.setPrefSize(40, 40);
        extendButton.setMaxSize(40, 40);
        extendButton.setCursor(Cursor.HAND);
        extendButton.getStyleClass().add("transparentButton");
        extendButton.setGraphic(extendIcon);
        extendButton.setVisible(false);
        extendButton.setOnAction(e -> extendMenu(menuState));


        Platform.runLater(() -> extendTooltip = new ControlTooltip(mainController, "Extend menu", extendButton, 1000, TooltipType.MENU_TOOLTIP));

        StackPane.setAlignment(extendButton, Pos.TOP_RIGHT);
        StackPane.setMargin(extendButton, new Insets(5, 5 , 0, 0));

        menuWrapper.setId("menuWrapper");

        menuContent.getChildren().addAll(closeButton, extendButton);


        dragPane.setCursor(Cursor.W_RESIZE);
        dragResizer = new DragResizer(this);

        metadataEditScroll.setVisible(false);
        metadataEditScroll.setBackground(Background.EMPTY);

        technicalDetailsScroll.setVisible(false);
        technicalDetailsScroll.setBackground(Background.EMPTY);

        chapterScroll.setVisible(false);
        chapterScroll.setBackground(Background.EMPTY);

        queueContainer.setVisible(false);
        queueContainer.setBackground(Background.EMPTY);

        settingsContainer.setVisible(false);
        settingsContainer.setBackground(Background.EMPTY);

        recentMediaContainer.setVisible(false);
        recentMediaContainer.setBackground(Background.EMPTY);

        musicLibraryContainer.setVisible(false);
        musicLibraryContainer.setBackground(Background.EMPTY);

        playlistsContainer.setVisible(false);
        playlistsContainer.setBackground(Background.EMPTY);
    }

    public void openMenu(MenuState newState) {

        if(        menuInTransition
                || controlBarController.durationSlider.isValueChanging()
                || controlBarController.volumeSlider.isValueChanging()
                || playbackSettingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.isValueChanging()
                || subtitlesController.subtitlesBox.subtitlesDragActive
                || playbackSettingsController.equalizerController.sliderActive) return;

        if(newState != menuState) updateState(newState);

        menuInTransition = true;

        mainController.videoImageViewWrapper.getScene().setCursor(Cursor.DEFAULT);

        subtitlesController.subtitlesBox.subtitlesContainer.setMouseTransparent(true);

        if(playbackSettingsController.playbackSettingsState != PlaybackSettingsState.CLOSED) playbackSettingsController.closeSettings();
        if (subtitlesController.subtitlesState != SubtitlesState.CLOSED) subtitlesController.closeSubtitles();

        if(extended.get()){
            if(controlBarController.controlBarOpen) AnimationsClass.hideTitle(mainController);
            else AnimationsClass.displayControls(controlBarController, subtitlesController, mainController);
            openExtendedMenu();
        }
        else {
            controlBarController.controlBarWrapper.setMouseTransparent(true);
            if(controlBarController.controlBarOpen) AnimationsClass.hideControlsAndTitle(controlBarController, subtitlesController, mainController);

            openShrinkedMenu();
        }
    }

    public void closeMenu(){

        if(menuInTransition) return;

        mainController.videoImageView.requestFocus();

        if(dragResizer.dragging) {
            dragResizer.dragging = false;
        }

        menuInTransition = true;
        menu.setMouseTransparent(true);

        if(extended.get()) closeExtendedMenu();
        else closeShrinkedMenu();

        mainController.videoTitleLabel.getScene().setCursor(Cursor.DEFAULT);
        mainController.videoTitleBox.setMouseTransparent(false);
        if(subtitlesController.subtitlesSelected.get()) subtitlesController.subtitlesBox.subtitlesContainer.setMouseTransparent(false);
    }


    public void init(MainController mainController, ControlBarController controlBarController, PlaybackSettingsController playbackSettingsController, MediaInterface mediaInterface, SubtitlesController subtitlesController, ChapterController chapterController){
        this.mainController = mainController;
        this.controlBarController = controlBarController;
        this.playbackSettingsController = playbackSettingsController;
        this.mediaInterface = mediaInterface;
        this.subtitlesController = subtitlesController;
        this.chapterController = chapterController;


        settingsPage.preferencesSection.loadLanguageBox();
    }

    public void extendMenu(MenuState newState){
        if(extended.get() || menuInTransition || menuState == MenuState.CLOSED) return;

        menu.setMouseTransparent(true);

        menuInTransition = true;

        Duration animationDuration = Duration.millis(300);
        Timeline maxTimeline = new Timeline(new KeyFrame(animationDuration,
                new KeyValue(menu.maxWidthProperty(), mainController.videoImageViewWrapper.getWidth() + 15, Interpolator.EASE_BOTH)));

        Timeline prefTimeline = new Timeline(new KeyFrame(animationDuration,
                new KeyValue(menu.prefWidthProperty(), mainController.videoImageViewWrapper.getWidth() + 15, Interpolator.EASE_BOTH)));

        FadeTransition menuContentFade = new FadeTransition(animationDuration, menuContent);
        menuContentFade.setFromValue(menuContent.getOpacity());
        menuContentFade.setToValue(0);

        ParallelTransition parallelWidth = new ParallelTransition(maxTimeline, prefTimeline);


        SequentialTransition sequentialTransition = new SequentialTransition(menuContentFade, parallelWidth);

        sequentialTransition.setOnFinished(e -> {
            setMenuExtended(newState);
            menu.setMouseTransparent(false);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), menuContent);
            fadeTransition.setFromValue(menuContent.getOpacity());
            fadeTransition.setToValue(1);
            fadeTransition.setOnFinished(ev -> {
                menuInTransition = false;
            });
            fadeTransition.play();
        });

        sequentialTransition.playFromStart();
    }

    public void shrinkMenu(){
        if(!extended.get() || menuInTransition || menuState == MenuState.CLOSED) return;

        menu.setMouseTransparent(true);
        menuInTransition = true;

        menu.maxWidthProperty().unbind();
        menu.prefWidthProperty().unbind();

        menu.setPrefWidth(mainController.videoImageViewWrapper.getWidth() + 15);
        menu.setMaxWidth(mainController.videoImageViewWrapper.getWidth() + 15);

        shrinkedWidth = Math.max(MIN_WIDTH, Math.min(shrinkedWidth, (mainController.videoImageViewWrapper.getWidth() + 30)/2));


        Duration animationDuration = Duration.millis(300);
        Timeline maxTimeline = new Timeline(new KeyFrame(animationDuration,
                new KeyValue(menu.maxWidthProperty(), shrinkedWidth, Interpolator.EASE_BOTH)));

        Timeline prefTimeline = new Timeline(new KeyFrame(animationDuration,
                new KeyValue(menu.prefWidthProperty(), shrinkedWidth, Interpolator.EASE_BOTH)));

        ParallelTransition parallelWidth = new ParallelTransition(maxTimeline, prefTimeline);

        parallelWidth.setOnFinished(e -> {

            extended.set(false);

            StackPane.setMargin(extendButton, new Insets(5, 5, 0, 0));

            extendButton.setOnAction(ev -> extendMenu(menuState));
            extendIcon.setShape(extendSVG);
            extendTooltip.updateText("Extend menu");
            extendButton.setVisible(true);
            extendButton.setMouseTransparent(false);

            queuePage.shrink();
            chapterController.chapterPage.shrink();
            menuBar.shrink();

            shrinkedWidth = Math.max(MIN_WIDTH, Math.min(shrinkedWidth, (mainController.videoImageViewWrapper.getWidth() + 30)/2));
            menu.setPrefWidth(shrinkedWidth);
            menu.setMaxWidth(shrinkedWidth);

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), menuContent);
            fadeTransition.setFromValue(menuContent.getOpacity());
            fadeTransition.setToValue(1);
            fadeTransition.setOnFinished(ev -> {
                menuInTransition = false;
                menu.setMouseTransparent(false);
            });
            fadeTransition.play();
        });

        FadeTransition menuContentFade = new FadeTransition(animationDuration, menuContent);
        menuContentFade.setFromValue(menuContent.getOpacity());
        menuContentFade.setToValue(0);
        menuContentFade.setOnFinished(e -> {

            controlBarController.controlBarWrapper.setViewOrder(2);
            menu.setViewOrder(1);

            StackPane.setMargin(menuWrapper, new Insets(0, 5, 0, 0));

            dragPane.setMouseTransparent(false);
            dragPane.setVisible(true);

            menuWrapper.setStyle("-fx-border-color: #909090;");
            menuWrapper.setPadding(Insets.EMPTY);

            parallelWidth.playFromStart();
        });

        menuContentFade.playFromStart();
        AnimationsClass.hideControlsAndTitle(controlBarController, subtitlesController, mainController);
    }

    public void setMenuExtended(MenuState newState){

        extended.set(true);

        if(newState != menuState) updateState(newState);

        StackPane.setMargin(extendButton, new Insets(10, 60, 0, 0));

        extendButton.setOnAction(e -> shrinkMenu());
        extendIcon.setShape(collapseSVG);
        extendTooltip.updateText("Collapse menu");
        if(newState == MenuState.QUEUE_OPEN || newState == MenuState.CHAPTERS_OPEN){
            extendButton.setVisible(true);
            extendButton.setMouseTransparent(false);
        }
        else {
            extendButton.setVisible(false);
            extendButton.setMouseTransparent(true);
        }

        menu.setTranslateX(0);
        if(menuState == MenuState.CLOSED) menu.setOpacity(0);
        menu.maxWidthProperty().bind(mainController.videoImageViewWrapper.widthProperty());
        menu.prefWidthProperty().bind(mainController.videoImageViewWrapper.widthProperty());

        dragPane.setMouseTransparent(true);
        dragPane.setVisible(false);
        StackPane.setMargin(menuWrapper, Insets.EMPTY);
        menuWrapper.setStyle("-fx-border-color: transparent;");

        if(mainController.videoImageViewWrapper.getWidth() >= 1200) {
            menuBar.extend();
        }

        queuePage.extend();
        chapterController.chapterPage.extend();

        controlBarController.controlBarWrapper.setMouseTransparent(false);
        controlBarController.controlBarWrapper.setViewOrder(1);
        menu.setViewOrder(2);

        menuWrapper.setPadding(new Insets(0, 0, 65, 0));

        if(menuState != MenuState.CLOSED){
            AnimationsClass.displayControls(controlBarController, subtitlesController, mainController);
        }
    }

    public void setMenuShrinked(){

        extended.set(false);

        StackPane.setMargin(extendButton, new Insets(5, 5, 0, 0));

        extendButton.setOnAction(e -> extendMenu(menuState));
        extendIcon.setShape(extendSVG);
        extendTooltip.updateText("Extend menu");
        extendButton.setVisible(true);
        extendButton.setMouseTransparent(false);

        menu.setOpacity(1);
        if(menuState == MenuState.CLOSED) menu.setTranslateX(-menu.getWidth());

        StackPane.setMargin(menuWrapper, new Insets(0, 5, 0, 0));
        menuWrapper.setPadding(Insets.EMPTY);

        controlBarController.controlBarWrapper.setViewOrder(2);
        menu.setViewOrder(1);

        queuePage.shrink();
        menuBar.shrink();
        chapterController.chapterPage.shrink();

        menu.maxWidthProperty().unbind();
        menu.prefWidthProperty().unbind();

        shrinkedWidth = Math.max(MIN_WIDTH, Math.min(shrinkedWidth, (mainController.videoImageViewWrapper.getWidth() + 30)/2));
        menu.setPrefWidth(shrinkedWidth);
        menu.setMaxWidth(shrinkedWidth);

        dragPane.setMouseTransparent(false);
        dragPane.setVisible(true);
        menuWrapper.setStyle("-fx-border-color: #909090;");

        if(controlBarController.controlBarOpen) AnimationsClass.hideControlsAndTitle(controlBarController, subtitlesController, mainController);
    }

    private void openExtendedMenu(){
        Duration animationDuration = Duration.millis(300);

        FadeTransition menuFade = new FadeTransition(animationDuration, menu);
        menuFade.setFromValue(menu.getOpacity());
        menuFade.setToValue(1);

        menuFade.setOnFinished(e -> {
            menu.setMouseTransparent(false);
            menuInTransition = false;
        });

        menuFade.play();
    }

    private void openShrinkedMenu(){
        TranslateTransition openMenu = new TranslateTransition(Duration.millis(300), menu);
        openMenu.setFromX(menu.getTranslateX());
        openMenu.setToX(0);
        openMenu.setInterpolator(Interpolator.EASE_OUT);

        openMenu.setOnFinished((e) -> {
            menu.setMouseTransparent(false);
            menuInTransition = false;
        });

        openMenu.play();
    }

    private void closeExtendedMenu(){

        Duration animationDuration = Duration.millis(300);

        FadeTransition menuFade = new FadeTransition(animationDuration, menu);
        menuFade.setFromValue(menu.getOpacity());
        menuFade.setToValue(0);

        menuFade.setOnFinished(e -> {
            menuInTransition = false;

            updateState(MenuState.CLOSED);

            controlBarController.mouseEventTracker.move();
        });

        menuFade.play();

        AnimationsClass.displayTitle(mainController);
    }

    private void closeShrinkedMenu(){
        TranslateTransition closeMenu = new TranslateTransition(Duration.millis(300), menu);
        closeMenu.setFromX(menu.getTranslateX());
        closeMenu.setToX(-menu.getWidth());

        closeMenu.setOnFinished((e) -> {
            menuInTransition = false;

            updateState(MenuState.CLOSED);

            controlBarController.mouseEventTracker.move();
        });
        closeMenu.play();
    }

    public void animateStateSwitch(MenuState newState){
        menuInTransition = true;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), menuContent);
        fadeOut.setFromValue(menuContent.getOpacity());
        fadeOut.setToValue(0);


        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), menuContent);
        fadeIn.setFromValue(menuContent.getOpacity());
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> menuInTransition = false);

        fadeOut.setOnFinished(e -> {
            updateState(newState);
            fadeIn.play();
        });

        fadeOut.play();
    }


    private void updateState(MenuState newState){
        MenuState oldState = this.menuState;

        this.menuState = newState;

        if(newState == MenuState.QUEUE_OPEN || newState == MenuState.CHAPTERS_OPEN){
            extendButton.setVisible(true);
            extendButton.setMouseTransparent(false);
        }
        else {
            extendButton.setVisible(false);
            extendButton.setMouseTransparent(true);
        }

        switch (oldState){
            case QUEUE_OPEN -> queuePage.closeQueuePage();
            case CHAPTERS_OPEN -> chapterController.chapterPage.closeChaptersPage();
            case SETTINGS_OPEN -> settingsPage.closeSettingsPage();
            case PLAYLISTS_OPEN -> playlistsPage.closePlaylistsPage();
            case RECENT_MEDIA_OPEN -> recentMediaPage.closeRecentMediaPage();
            case METADATA_EDIT_OPEN -> metadataEditPage.closeMetadataEditPage();
            case MUSIC_LIBRARY_OPEN -> musicLibraryPage.closeMusicLibraryPage();
            case TECHNICAL_DETAILS_OPEN -> technicalDetailsPage.closeTechnicalDetailsPage();
        }

        switch (newState){
            case QUEUE_OPEN -> queuePage.openQueuePage();
            case CHAPTERS_OPEN -> chapterController.chapterPage.openChaptersPage();
            case SETTINGS_OPEN -> settingsPage.openSettingsPage();
            case PLAYLISTS_OPEN -> playlistsPage.openPlaylistsPage();
            case RECENT_MEDIA_OPEN -> recentMediaPage.openRecentMediaPage();
            case METADATA_EDIT_OPEN -> metadataEditPage.openMetadataEditPage();
            case MUSIC_LIBRARY_OPEN -> musicLibraryPage.openMusicLibraryPage();
            case TECHNICAL_DETAILS_OPEN -> technicalDetailsPage.openTechnicalDetailsPage();
        }
    }


}



