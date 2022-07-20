package hans;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.util.*;


import io.github.palexdev.materialfx.utils.SwingFXUtils;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;


import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;


import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;

import static hans.SVG.*;


public class MainController implements Initializable {

    @FXML
    public MediaView mediaView;

    @FXML
    Button menuButton;

    @FXML
    StackPane outerPane, menuButtonPane, mediaViewWrapper, mediaViewInnerWrapper;

    @FXML
    BorderPane mainPane;

    @FXML
    Region menuIcon;

    @FXML
    private ControlBarController controlBarController;

    @FXML
    private MenuController menuController;

    SettingsController settingsController;
    CaptionsController captionsController;

    MediaInterface mediaInterface;

    DoubleProperty mediaViewWidth;
    DoubleProperty mediaViewHeight;


    // counter to keep track of the current node that has focus (used for focus traversing with tab and shift+tab)
    public int focusNodeTracker = 0;

    ControlTooltip openMenuTooltip;

    SVGPath menuSVG;

    ActionIndicator actionIndicator;
    SeekIndicator forwardsIndicator, backwardsIndicator;
    ValueIndicator valueIndicator;

    SimpleDoubleProperty sizeMultiplier = new SimpleDoubleProperty();

    StackPane whitePane = new StackPane();
    Label miniplayerActiveText = new Label();


    boolean miniplayerActive = false;
    Miniplayer miniplayer;

    ChangeListener<? super Number> widthListener;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        settingsController = new SettingsController(this, controlBarController, menuController);
        mediaInterface = new MediaInterface(this, controlBarController, settingsController, menuController);
        captionsController = new CaptionsController(settingsController, this, mediaInterface, controlBarController, menuController);


        controlBarController.init(this, settingsController, menuController, mediaInterface, captionsController); // shares references of all the controllers between eachother
        menuController.init(this, controlBarController, settingsController, mediaInterface, captionsController);
        settingsController.init(mediaInterface, captionsController);

        mediaViewWrapper.getChildren().add(2, settingsController.settingsBuffer);


        // declaring media control images
        menuSVG = new SVGPath();
        menuSVG.setContent(App.svgMap.get(MENU));

        sizeMultiplier.set(0.65);

        actionIndicator = new ActionIndicator(this);
        forwardsIndicator = new SeekIndicator(this, true);
        backwardsIndicator = new SeekIndicator(this, false);
        valueIndicator = new ValueIndicator(this);


        // Make mediaView adjust to frame size

        mediaViewWidth = mediaView.fitWidthProperty();
        mediaViewHeight = mediaView.fitHeightProperty();
        mediaViewWidth.bind(mediaViewInnerWrapper.widthProperty());
        Platform.runLater(() -> mediaViewHeight.bind(mediaViewInnerWrapper.getScene().heightProperty()));

        mediaView.setPreserveRatio(true);


        //video expands to take up entire window if menu is not open
        Platform.runLater(() ->{
            if(!menuController.menuOpen){
                mediaViewWrapper.prefWidthProperty().bind(mediaViewWrapper.getScene().widthProperty());
            }
        });



        mediaViewWrapper.setStyle("-fx-background-color: rgb(0,0,0)");
        mediaViewInnerWrapper.setStyle("-fx-background-color: rgb(0,0,0)");

        menuButtonPane.translateXProperty().bind(menuController.menu.widthProperty().multiply(-1));
        menuButton.setBackground(Background.EMPTY);
        menuButton.setVisible(false);

        menuButtonPane.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            controlBarController.controlButtonHoverOn(menuButtonPane);
        });

        menuButtonPane.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            controlBarController.controlButtonHoverOff(menuButtonPane);
        });

        menuIcon.setShape(menuSVG);

        whitePane.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        whitePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        whitePane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        whitePane.setMouseTransparent(true);
        whitePane.setOpacity(0);



        miniplayerActiveText.setText("Video active in miniplayer");
        miniplayerActiveText.setId("mediaViewText");
        miniplayerActiveText.setBackground(Background.EMPTY);
        miniplayerActiveText.setMouseTransparent(true);
        miniplayerActiveText.setVisible(false);
        StackPane.setAlignment(miniplayerActiveText, Pos.CENTER);

        mediaViewWrapper.getChildren().add(whitePane);
        mediaViewInnerWrapper.getChildren().addAll(miniplayerActiveText);

        Platform.runLater(() -> {            // needs to be run later so that the rest of the app can load in and this tooltip popup has a parent window to be associated with
            openMenuTooltip = new ControlTooltip("Open menu (q)", menuButton, controlBarController.controlBarWrapper, 1000, true);

            mediaViewWrapper.sceneProperty().get().widthProperty().addListener((observableValue, oldValue, newValue) -> {
                if(newValue.doubleValue() < menuController.menu.getPrefWidth()){
                    menuController.menu.setPrefWidth(newValue.doubleValue());
                    menuController.prefWidth = newValue.doubleValue();
                }
            });


        });

        widthListener = (observableValue, oldValue, newValue) -> {

            if(oldValue.doubleValue() >= 800 && newValue.doubleValue() < 800){
                captionsController.mediaWidthMultiplier.set(0.4);
                captionsController.resizeCaptions();

                sizeMultiplier.set(0.55);
                if(actionIndicator.wrapper.isVisible()) actionIndicator.updateSize();
                forwardsIndicator.resize();
                backwardsIndicator.resize();
                valueIndicator.resize();
            }
            else if((oldValue.doubleValue() < 800 || oldValue.doubleValue() >= 1200) && (newValue.doubleValue() >= 800 && newValue.doubleValue() < 1200)){
                captionsController.mediaWidthMultiplier.set(0.6);
                captionsController.resizeCaptions();

                sizeMultiplier.set(0.65);
                if(actionIndicator.wrapper.isVisible()) actionIndicator.updateSize();
                forwardsIndicator.resize();
                backwardsIndicator.resize();
                valueIndicator.resize();

            }
            else if((oldValue.doubleValue() < 1200 || oldValue.doubleValue() >= 1800) && (newValue.doubleValue() >= 1200 && newValue.doubleValue() < 1800)){
                captionsController.mediaWidthMultiplier.set(0.8);
                captionsController.resizeCaptions();

                sizeMultiplier.set(0.8);
                if(actionIndicator.wrapper.isVisible()) actionIndicator.updateSize();
                forwardsIndicator.resize();
                backwardsIndicator.resize();
                valueIndicator.resize();

            }
            else if((oldValue.doubleValue() < 1800 || oldValue.doubleValue() >= 2400) && (newValue.doubleValue() >= 1800 && newValue.doubleValue() < 2400)){
                captionsController.mediaWidthMultiplier.set(1.0);
                captionsController.resizeCaptions();


                sizeMultiplier.set(1);
                if(actionIndicator.wrapper.isVisible()) actionIndicator.updateSize();
                forwardsIndicator.resize();
                backwardsIndicator.resize();
                valueIndicator.resize();

            }
            else if(oldValue.doubleValue() < 2400 && newValue.doubleValue() >= 2400){
                captionsController.mediaWidthMultiplier.set(1.2);
                captionsController.resizeCaptions();

                sizeMultiplier.set(1.2);
                if(actionIndicator.wrapper.isVisible()) actionIndicator.updateSize();
                forwardsIndicator.resize();
                backwardsIndicator.resize();
                valueIndicator.resize();
            }
        };

        mediaViewInnerWrapper.widthProperty().addListener(widthListener);

        mediaViewInnerWrapper.setOnMouseDragOver(e -> {
            if(captionsController.captionsDragActive){
                if(e.getY() - captionsController.dragPositionY <= captionsController.minimumY) captionsController.captionsBox.setTranslateY(((captionsController.startY - captionsController.minimumY) * -1) + captionsController.startTranslateY);
                else if(e.getY() - captionsController.dragPositionY + captionsController.captionsBox.getLayoutBounds().getMaxY() > captionsController.maximumY) captionsController.captionsBox.setTranslateY(captionsController.maximumY - captionsController.startY - captionsController.captionsBox.getLayoutBounds().getMaxY() + captionsController.startTranslateY);
                else captionsController.captionsBox.setTranslateY(e.getY() - captionsController.dragPositionY - captionsController.startY + captionsController.startTranslateY);

                if(e.getX() - captionsController.dragPositionX <= captionsController.minimumX) captionsController.captionsBox.setTranslateX(((captionsController.startX - captionsController.minimumX) * -1) + captionsController.startTranslateX);
                else if(e.getX() - captionsController.dragPositionX + captionsController.captionsBox.getLayoutBounds().getMaxX() > captionsController.maximumX) captionsController.captionsBox.setTranslateX(captionsController.maximumX - captionsController.startX - captionsController.captionsBox.getLayoutBounds().getMaxX() + captionsController.startTranslateX);
                else captionsController.captionsBox.setTranslateX(e.getX() - captionsController.dragPositionX - captionsController.startX + captionsController.startTranslateX);
            }
        });

        mediaView.focusedProperty()
                .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (!newValue) {
                        mediaView.setStyle("-fx-border-color: transparent;");
                    } else {
                        focusNodeTracker = 0;
                    }
                });

        menuButton.focusedProperty()
                .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (!newValue) {
                        menuButton.setStyle("-fx-border-color: transparent;");
                    } else {
                        focusNodeTracker = 8;
                    }
                });


        mediaView.setOnMouseClicked(e -> {

            if (e.getClickCount() == 1) {
                mediaClick();
            }

            if (e.getClickCount() == 2) {
                mediaClick();
                controlBarController.fullScreen();
            }
        });



    }

    public void mediaClick() {

        // Clicking on the mediaview node will close the settings tab if its open or
        // otherwise play/pause/replay the video

        if (settingsController.settingsState != SettingsState.CLOSED) {
            settingsController.closeSettings();
        }
        else if(mediaInterface.mediaPlayer != null && !miniplayerActive){
            if (mediaInterface.atEnd) {
                controlBarController.replayMedia();
                actionIndicator.setIcon(REPLAY);
            } else {
                if (mediaInterface.playing.get()) {
                    controlBarController.pause();
                    actionIndicator.setIcon(PAUSE);
                } else {
                    controlBarController.play();
                    actionIndicator.setIcon(PLAY);
                }
            }
            actionIndicator.setVisible(true);
            actionIndicator.animate();
        }

        mediaView.requestFocus();
    }


    public void openMenu() {
        if(!menuController.menuInTransition) {

            captionsController.cancelDrag();

            menuController.menuInTransition = true;
            menuController.menuOpen = true;

            AnimationsClass.openMenu(this, menuController);
        }
    }

    public void handleDragEntered(DragEvent e){
        File file = e.getDragboard().getFiles().get(0);
        if(!Utilities.getFileExtension(file).equals("mp4") && !Utilities.getFileExtension(file).equals("mp3")) return;


        actionIndicator.setIcon(PLUS);
        actionIndicator.setVisible(true);

        if(settingsController.settingsState != SettingsState.CLOSED) settingsController.closeSettings();

       if(mediaInterface.playing.get())controlBarController.mouseEventTracker.hide();
       else AnimationsClass.hideControls(controlBarController, captionsController);

    }

    public void handleDragExited(DragEvent e){

        if(actionIndicator.parallelTransition.getStatus() != Animation.Status.RUNNING) actionIndicator.setVisible(false);
    }

    public void handleDragOver(DragEvent e){
        File file = e.getDragboard().getFiles().get(0);
        if(!Utilities.getFileExtension(file).equals("mp4") && !Utilities.getFileExtension(file).equals("mp3")) return;

        e.acceptTransferModes(TransferMode.COPY);
    }

    public void handleDragDropped(DragEvent e){
        //mediaView.setEffect(null);
        File file = e.getDragboard().getFiles().get(0);

        /* return statement */
        if(!Utilities.getFileExtension(file).equals("mp4") && !Utilities.getFileExtension(file).equals("mp3")) return;

        actionIndicator.animate();


        MediaItem temp = null;

        if(Utilities.getFileExtension(file).equals("mp4")) temp = new Mp4Item(file);
        else if(Utilities.getFileExtension(file).equals("mp3")) temp = new Mp3Item(file);

        ActiveItem activeItem = new ActiveItem(temp, menuController, mediaInterface, menuController.activeBox);
        activeItem.play(true);

    }

    public void takeScreenshot(){
        if(menuController.activeItem == null || miniplayerActive) return;

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG file (*.png)", "*.png"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG file (*.jpg)", "*.jpg"));

        double width = menuController.activeItem.getMediaItem().getMedia().getWidth();
        double height = menuController.activeItem.getMediaItem().getMedia().getHeight();
        double aspectRatio = width / height;

        double realWidth = Math.min(mediaView.getFitWidth(), mediaView.getFitHeight() * aspectRatio);
        double realHeight = Math.min(mediaView.getFitHeight(), mediaView.getFitWidth() / aspectRatio);

        WritableImage writableImage = new WritableImage((int) realWidth, (int)realHeight);

        mediaView.snapshot(null, writableImage);

        // Flashing screen animation
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), whitePane);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(2);
        fadeTransition.playFromStart();


        //Prompt user to select a file
        File file = fileChooser.showSaveDialog(App.stage);

        if(file != null){
            try {
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, new BufferedImage((int) writableImage.getWidth(), (int) writableImage.getHeight(), BufferedImage.TYPE_INT_RGB));

                //Write the snapshot to the chosen file
                ImageIO.write(renderedImage, Utilities.getFileExtension(file), file);

            } catch (IOException ex) { ex.printStackTrace(); }
        }
    }

    public void openMiniplayer(){
        miniplayerActive = true;

        mediaView.setMediaPlayer(null);
        // causes Concurrent Modification Exception
        // seems to be a JavaFX bug, have to try either creating new mediaviews every opening/closing of the miniplayer or implementing vlcj library

        mediaViewInnerWrapper.widthProperty().removeListener(widthListener);

        miniplayer = new Miniplayer(this, controlBarController, menuController, mediaInterface, settingsController);

        miniplayer.miniplayerController.moveIndicators();
        captionsController.moveToMiniplayer();

        if(menuController.activeItem != null){
            miniplayerActiveText.setVisible(true);
        }
    }

    public void closeMiniplayer(){

        miniplayer.miniplayerController.mediaViewInnerWrapper.widthProperty().removeListener(miniplayer.miniplayerController.widthListener);

        actionIndicator.moveToMainplayer();
        forwardsIndicator.moveToMainplayer();
        backwardsIndicator.moveToMainplayer();
        valueIndicator.moveToMainplayer();

        captionsController.moveToMainplayer();

        if(miniplayerActive && miniplayer != null && miniplayer.stage != null){
            miniplayer.stage.close();
        }

        miniplayerActive = false;

        resizeIndicators();
        mediaViewInnerWrapper.widthProperty().addListener(widthListener);

        if(menuController.activeItem != null && mediaInterface.mediaPlayer != null){
            mediaView.setMediaPlayer(mediaInterface.mediaPlayer);
            miniplayerActiveText.setVisible(false);
        }
    }

    public void resizeIndicators(){
        if(mediaViewInnerWrapper.getWidth() < 800){
            captionsController.mediaWidthMultiplier.set(0.4);
            captionsController.resizeCaptions();

            sizeMultiplier.set(0.55);
            forwardsIndicator.resize();
            backwardsIndicator.resize();
            valueIndicator.resize();
        }
        else if(mediaViewInnerWrapper.getWidth() >= 800 && mediaViewInnerWrapper.getWidth() < 1200){
            captionsController.mediaWidthMultiplier.set(0.6);
            captionsController.resizeCaptions();

            sizeMultiplier.set(0.65);
            forwardsIndicator.resize();
            backwardsIndicator.resize();
            valueIndicator.resize();

        }
        else if(mediaViewInnerWrapper.getWidth() >= 1200 && mediaViewInnerWrapper.getWidth() < 1800){
            captionsController.mediaWidthMultiplier.set(0.8);
            captionsController.resizeCaptions();

            sizeMultiplier.set(0.8);
            forwardsIndicator.resize();
            backwardsIndicator.resize();
            valueIndicator.resize();

        }
        else if(mediaViewInnerWrapper.getWidth() >= 1800 && mediaViewInnerWrapper.getWidth() < 2400){
            captionsController.mediaWidthMultiplier.set(1.0);
            captionsController.resizeCaptions();


            sizeMultiplier.set(1);
            forwardsIndicator.resize();
            backwardsIndicator.resize();
            valueIndicator.resize();

        }
        else if(mediaViewInnerWrapper.getWidth() >= 2400){
            captionsController.mediaWidthMultiplier.set(1.2);
            captionsController.resizeCaptions();

            sizeMultiplier.set(1.2);
            forwardsIndicator.resize();
            backwardsIndicator.resize();
            valueIndicator.resize();
        }
    }


    public void pressRIGHT(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(settingsController.settingsState == SettingsState.CUSTOM_SPEED_OPEN){
            // if custom speed pane is open, dont seek video with arrows
            settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.setValueChanging(true);
            settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.setValue(settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.getValue() + 0.05);
            e.consume();
            return;
        }

        if (!controlBarController.volumeSlider.isFocused() && menuController.mediaActive.get()) {

            if(backwardsIndicator.wrapper.isVisible()){
                backwardsIndicator.setVisible(false);
            }
            forwardsIndicator.setText("5 seconds");
            forwardsIndicator.reset();
            forwardsIndicator.setVisible(true);
            forwardsIndicator.animate();

            if (mediaInterface.mediaPlayer.getCurrentTime().toSeconds() + 5 >= controlBarController.durationSlider.getMax()) {
                mediaInterface.seekedToEnd = true;
            }

            controlBarController.durationSlider.setValue(controlBarController.durationSlider.getValue() + 5);

            e.consume();

        }
    }

    public void pressLEFT(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(settingsController.settingsState == SettingsState.CUSTOM_SPEED_OPEN){
            // if custom speed pane is open, dont seek video with arrows
            settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.setValueChanging(true);
            settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.setValue(settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.getValue() - 0.05);
            e.consume();
            return;
        }

        if (!controlBarController.volumeSlider.isFocused() && menuController.mediaActive.get()) {

            if(forwardsIndicator.wrapper.isVisible()){
                forwardsIndicator.setVisible(false);
            }
            backwardsIndicator.setText("5 seconds");
            backwardsIndicator.reset();
            backwardsIndicator.setVisible(true);
            backwardsIndicator.animate();

            mediaInterface.seekedToEnd = false;

            controlBarController.durationSlider.setValue(controlBarController.durationSlider.getValue() - 5);
            e.consume();

        }
    }

    public void pressTAB(KeyEvent e){
        controlBarController.mouseEventTracker.move();
    }

    public void pressM(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if (!controlBarController.muted) {
            controlBarController.mute();
            actionIndicator.setIcon(VOLUME_MUTED);
        } else {
            controlBarController.unmute();
            actionIndicator.setIcon(VOLUME_HIGH);
        }

        valueIndicator.setValue((int) (controlBarController.volumeSlider.getValue()) + "%");
        valueIndicator.play();

        actionIndicator.setVisible(true);
        actionIndicator.animate();
    }

    public void pressSPACE(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if (!controlBarController.durationSlider.isValueChanging() && menuController.mediaActive.get()) { // wont let user play/pause video while media slider is seeking
            if (mediaInterface.atEnd) {
                controlBarController.replayMedia();
                actionIndicator.setIcon(REPLAY);
            } else {
                if (mediaInterface.playing.get()) {
                    controlBarController.pause();
                    actionIndicator.setIcon(PAUSE);
                } else {
                    controlBarController.play();
                    actionIndicator.setIcon(PLAY);
                }
            }
            actionIndicator.setVisible(true);
            actionIndicator.animate();

            e.consume(); // might have to add a check to consume the space event only if any controlbar buttons are focused (might use space bar to navigate settings or menu)
        }
    }

    public void pressJ(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(forwardsIndicator.wrapper.isVisible()){
            forwardsIndicator.setVisible(false);
        }
        backwardsIndicator.setText("10 seconds");
        backwardsIndicator.reset();
        backwardsIndicator.setVisible(true);
        backwardsIndicator.animate();

        if (menuController.mediaActive.get()) {
            mediaInterface.seekedToEnd = false;
            controlBarController.durationSlider.setValue(controlBarController.durationSlider.getValue() - 10.0);
        }
    }

    public void pressK(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if (!controlBarController.durationSlider.isValueChanging() && menuController.mediaActive.get()) {  // wont let user play/pause video while media slider is seeking
            if (mediaInterface.atEnd) {
                controlBarController.replayMedia();
                actionIndicator.setIcon(REPLAY);
            } else {
                if (mediaInterface.playing.get()) {
                    controlBarController.pause();
                    actionIndicator.setIcon(PAUSE);
                } else {
                    controlBarController.play();
                    actionIndicator.setIcon(PLAY);
                }
            }
            actionIndicator.setVisible(true);
            actionIndicator.animate();
        }
    }

    public void pressL(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(backwardsIndicator.wrapper.isVisible()){
            backwardsIndicator.setVisible(false);
        }
        forwardsIndicator.setText("10 seconds");
        forwardsIndicator.reset();
        forwardsIndicator.setVisible(true);
        forwardsIndicator.animate();

        if (menuController.mediaActive.get()) {

            if (mediaInterface.mediaPlayer.getCurrentTime().toSeconds() + 10 >= controlBarController.durationSlider.getMax()) {
                mediaInterface.seekedToEnd = true;
            }
            controlBarController.durationSlider.setValue(controlBarController.durationSlider.getValue() + 10);
            e.consume();

        }
    }

    public void pressCOMMA(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(e.isShiftDown()){ // decrease playback speed by 0.25
            settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.setValueChanging(true);
            settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.setValue(settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.getValue() - 0.25);

            valueIndicator.setValue(settingsController.playbackSpeedController.df.format(settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.getValue()) + "x");
            valueIndicator.play();

            actionIndicator.setIcon(REWIND);
            actionIndicator.setVisible(true);
            actionIndicator.animate();


            // also show new playback speed as a label top center of the mediaviewpane

            e.consume();
            return;
        }

        // seek backwards by 1 frame
        if(!mediaInterface.playing.get() && menuController.mediaActive.get()) {
            mediaInterface.seekedToEnd = false;
            mediaInterface.mediaPlayer.seek(mediaInterface.mediaPlayer.getCurrentTime().subtract(Duration.seconds(App.frameDuration)));
        }
        e.consume();
    }

    public void pressPERIOD(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(e.isShiftDown()){ // increase playback speed by 0.25
            settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.setValueChanging(true);
            settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.setValue(settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.getValue() + 0.25);

            valueIndicator.setValue(settingsController.playbackSpeedController.df.format(settingsController.playbackSpeedController.customSpeedPane.customSpeedSlider.getValue()) + "x");
            valueIndicator.play();

            actionIndicator.setIcon(FORWARD);
            actionIndicator.setVisible(true);
            actionIndicator.animate();


            // also show new playback speed as a label top center of the mediaviewpane

            e.consume();
            return;
        }

        // seek forward by 1 frame
        if(!mediaInterface.playing.get() && menuController.mediaActive.get()){
            if (mediaInterface.mediaPlayer.getCurrentTime().toSeconds() + App.frameDuration >= controlBarController.durationSlider.getMax()) {
                mediaInterface.seekedToEnd = true;
            }

            mediaInterface.mediaPlayer.seek(mediaInterface.mediaPlayer.getCurrentTime().add(Duration.seconds(App.frameDuration)));
        }
        e.consume();
    }

    public void pressUP(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        controlBarController.volumeSlider.setValue(Math.min(controlBarController.volumeSlider.getValue() + 5, 100));
        valueIndicator.setValue((int) (controlBarController.volumeSlider.getValue()) + "%");

        valueIndicator.play();

        actionIndicator.setIcon(VOLUME_HIGH);
        actionIndicator.setVisible(true);
        actionIndicator.animate();
    }

    public void pressDOWN(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        controlBarController.volumeSlider.setValue(Math.max(controlBarController.volumeSlider.getValue() - 5, 0));
        valueIndicator.setValue((int) (controlBarController.volumeSlider.getValue()) + "%");

        valueIndicator.play();

        if(controlBarController.volumeSlider.getValue() == 0) actionIndicator.setIcon(VOLUME_MUTED);
        else actionIndicator.setIcon(VOLUME_LOW);
        actionIndicator.setVisible(true);
        actionIndicator.animate();
    }

    public void pressI(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(miniplayerActive) closeMiniplayer();
        else openMiniplayer();
    }

    public void pressQ(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(menuController.menuOpen) menuController.closeMenu();
        else openMenu();
    }

    public void pressS(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if (settingsController.settingsState != SettingsState.CLOSED) {
            settingsController.closeSettings();
        } else {
            settingsController.openSettings();
        }
    }

    public void pressN(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(e.isShiftDown()){

            if((menuController.historyBox.index != -1 && menuController.historyBox.index < menuController.history.size() -1) || ((menuController.historyBox.index == menuController.history.size() -1 || menuController.historyBox.index == -1) && !menuController.queue.isEmpty())){

                if(!menuController.animationsInProgress.isEmpty()) return;

                actionIndicator.setIcon(NEXT_VIDEO);
                actionIndicator.setVisible(true);
                actionIndicator.animate();

                mediaInterface.playNext();
            }

        }
    }

    public void pressP(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(e.isShiftDown()){

            if(menuController.mediaActive.get() && controlBarController.durationSlider.getValue() > 5){ // restart current video
                actionIndicator.setIcon(REPLAY);
                actionIndicator.setVisible(true);
                actionIndicator.animate();

                mediaInterface.seekedToEnd = false;
                controlBarController.durationSlider.setValue(0);

            }
            else if((!menuController.history.isEmpty() && menuController.historyBox.index == -1) || menuController.historyBox.index > 0){ // play previous video

                if(!menuController.animationsInProgress.isEmpty()) return;

                actionIndicator.setIcon(PREVIOUS_VIDEO);
                actionIndicator.setVisible(true);
                actionIndicator.animate();

                mediaInterface.playPrevious();
            }

        }
    }

    public void pressF(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        controlBarController.fullScreen();
    }

    public void pressF12(KeyEvent e){
        takeScreenshot();
        controlBarController.mouseEventTracker.move();
    }

    public void pressC(KeyEvent e){
        controlBarController.mouseEventTracker.move();

        if(!captionsController.captionsSelected || captionsController.captionsDragActive) return;

        if (captionsController.captionsOn.get()) {
            controlBarController.closeCaptions();
        } else {
            controlBarController.openCaptions();
        }

        captionsController.captionsPane.captionsToggle.fire();
    }

    public void press1(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(menuController.mediaActive.get()) controlBarController.durationSlider.setValue(menuController.activeItem.mediaItem.getMedia().getDuration().toSeconds() * 1 / 10);
    }

    public void press2(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(menuController.mediaActive.get()) controlBarController.durationSlider.setValue(menuController.activeItem.mediaItem.getMedia().getDuration().toSeconds() * 2 / 10);
    }

    public void press3( KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(menuController.mediaActive.get()) controlBarController.durationSlider.setValue(menuController.activeItem.mediaItem.getMedia().getDuration().toSeconds() * 3 / 10);
    }

    public void press4(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(menuController.mediaActive.get()) controlBarController.durationSlider.setValue(menuController.activeItem.mediaItem.getMedia().getDuration().toSeconds() * 4 / 10);
    }

    public void press5(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(menuController.mediaActive.get()) controlBarController.durationSlider.setValue(menuController.activeItem.mediaItem.getMedia().getDuration().toSeconds() * 5 / 10);
    }

    public void press6(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(menuController.mediaActive.get()) controlBarController.durationSlider.setValue(menuController.activeItem.mediaItem.getMedia().getDuration().toSeconds() * 6 / 10);
    }

    public void press7(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(menuController.mediaActive.get()) controlBarController.durationSlider.setValue(menuController.activeItem.mediaItem.getMedia().getDuration().toSeconds() * 7 / 10);
    }

    public void press8(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(menuController.mediaActive.get()) controlBarController.durationSlider.setValue(menuController.activeItem.mediaItem.getMedia().getDuration().toSeconds() * 8 / 10);
    }

    public void press9(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        if(menuController.mediaActive.get()) controlBarController.durationSlider.setValue(menuController.activeItem.mediaItem.getMedia().getDuration().toSeconds() * 9 / 10);
    }

    public void press0(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        mediaInterface.seekedToEnd = false;
        if(menuController.mediaActive.get()){
            controlBarController.durationSlider.setValue(0);
            actionIndicator.setIcon(REPLAY);
            actionIndicator.setVisible(true);
            actionIndicator.animate();
        }
    }

    public void pressESCAPE(KeyEvent e){
        captionsController.cancelDrag();

        controlBarController.mouseEventTracker.move();
        if (settingsController.settingsState != SettingsState.CLOSED && !App.fullScreen) {
            settingsController.closeSettings();
        }
        App.fullScreen = false;

        controlBarController.fullScreenIcon.setShape(controlBarController.maximizeSVG);
        App.stage.setFullScreen(false);

        if (settingsController.settingsState == SettingsState.CLOSED)
            controlBarController.fullScreen = new ControlTooltip("Full screen (f)", controlBarController.fullScreenButton, controlBarController.controlBarWrapper, 0, false);
    }

    public void pressEND(KeyEvent e){
        controlBarController.mouseEventTracker.move();
        mediaInterface.seekedToEnd = true;
        if(menuController.mediaActive.get()){
            controlBarController.durationSlider.setValue(controlBarController.durationSlider.getMax());
            actionIndicator.setIcon(NEXT_VIDEO);
            actionIndicator.setVisible(true);
            actionIndicator.animate();
        }
    }



    public SettingsController getSettingsController() {
        return settingsController;
    }

    public ControlBarController getControlBarController() {
        return controlBarController;
    }

    public MenuController getMenuController(){ return menuController;}

    public MediaInterface getMediaInterface() {
        return mediaInterface;
    }

    public CaptionsController getCaptionsController(){
        return captionsController;
    }
}