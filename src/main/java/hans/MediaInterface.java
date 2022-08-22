package hans;


import io.github.palexdev.materialfx.font.MFXFontIcon;
import javafx.animation.PauseTransition;
import javafx.application.Platform;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.SubtitleTrack;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;


import java.awt.*;
import java.io.File;
import java.util.*;



public class MediaInterface {

    MainController mainController;
    ControlBarController controlBarController;
    SettingsController settingsController;
    MenuController menuController;

    public MediaPlayerFactory mediaPlayerFactory;

    public EmbeddedMediaPlayer embeddedMediaPlayer;
    public MediaItem activeMediaItem;

    // Variables to keep track of mediaplayer status:
    BooleanProperty mediaActive = new SimpleBooleanProperty(false); // is the mediaplayer active (is any video currently loaded in)
    BooleanProperty playing = new SimpleBooleanProperty(false); // is mediaplayer currently playing
    boolean wasPlaying = false; // was mediaplayer playing before a seeking action occurred
    public boolean atEnd = false; // is mediaplayer at the end of the video
    public boolean seekedToEnd = false; // true = video was seeked to the end; false = video naturally reached the end or the video is still playing
    ////////////////////////////////////////////////


    PauseTransition transitionTimer;

    double currentTime;


    MediaInterface(MainController mainController, ControlBarController controlBarController, SettingsController settingsController, MenuController menuController) {
        this.mainController = mainController;
        this.controlBarController = controlBarController;
        this.settingsController = settingsController;
        this.menuController = menuController;


        mediaActive.addListener((observableValue, oldValue, newValue) -> {
            controlBarController.durationPane.setMouseTransparent(!newValue);
            if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.sliderPane.setMouseTransparent(!newValue);


            if(newValue){
                if(!controlBarController.playButtonEnabled) controlBarController.enablePlayButton();
                if(mainController.miniplayerActive){
                    mainController.miniplayer.miniplayerController.enablePlayButton();
                    mainController.miniplayerActiveText.setVisible(true);
                }
            }
            else {
                if(controlBarController.playButtonEnabled) controlBarController.disablePlayButton();
                if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.disablePlayButton();
                mainController.miniplayerActiveText.setVisible(false);
            }
        });
    }

    public void init(){
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();


        embeddedMediaPlayer.videoSurface().set(new ImageViewVideoSurface(mainController.videoImageView));
        embeddedMediaPlayer.audio().setVolume((int) controlBarController.volumeSlider.getValue());

        embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {


            @Override
            public void finished(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> {
                    controlBarController.durationSlider.setValue(controlBarController.durationSlider.getMax());
                });
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer){
                // mediaplayer is stoppped when opening/closing miniplayer, have to make sure this code here doesnt mess with that
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {

                currentTime = newTime;

                Platform.runLater(() ->{
                    if(Math.abs(currentTime/1000 - controlBarController.durationSlider.getValue()) > 0.5)controlBarController.durationSlider.setValue((double)newTime/1000);

                    if(!menuController.captionsController.subtitles.isEmpty() &&
                            menuController.captionsController.captionsPosition >= 0 &&
                            menuController.captionsController.captionsPosition < menuController.captionsController.subtitles.size() &&
                            menuController.captionsController.captionsOn.get() &&
                            !menuController.captionsController.captionsDragActive){


                        if(newTime >= menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition).timeIn && newTime < menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition).timeOut && !menuController.captionsController.showedCurrentCaption){
                            String text = menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition).text;

                            // if the subtitle contains a new line character then split the subtitle into two and add the part after the new line onto another label

                            String[] subtitleLines = Utilities.splitLines(text);

                            if(subtitleLines.length == 2){
                                menuController.captionsController.captionsLabel1.setOpacity(1);
                                menuController.captionsController.captionsLabel2.setOpacity(1);
                                menuController.captionsController.captionsLabel1.setText(subtitleLines[0]);
                                menuController.captionsController.captionsLabel2.setText(subtitleLines[1]);
                            }
                            else {
                                menuController.captionsController.captionsLabel1.setOpacity(0);
                                menuController.captionsController.captionsLabel2.setOpacity(1);
                                menuController.captionsController.captionsLabel2.setText(subtitleLines[0]);
                            }

                            menuController.captionsController.showedCurrentCaption = true;
                        }
                        else if((newTime >= menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition).timeOut && menuController.captionsController.captionsPosition >= menuController.captionsController.subtitles.size() - 1) || (newTime) >= menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition).timeOut && newTime < menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition + 1).timeIn){
                            menuController.captionsController.captionsLabel1.setOpacity(0);
                            menuController.captionsController.captionsLabel2.setOpacity(0);
                        }
                        else if(newTime < menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition).timeIn && menuController.captionsController.captionsPosition > 0){
                            do {
                                menuController.captionsController.captionsPosition--;
                                menuController.captionsController.showedCurrentCaption = false;
                            }
                            while (newTime < menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition).timeIn && menuController.captionsController.captionsPosition > 0);
                        }
                        else if(menuController.captionsController.captionsPosition <  menuController.captionsController.subtitles.size() - 1 && newTime >= menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition + 1).timeIn){
                            do {
                                menuController.captionsController.captionsPosition++;
                                menuController.captionsController.showedCurrentCaption = false;
                            }
                            while (menuController.captionsController.captionsPosition <  menuController.captionsController.subtitles.size() - 1 && newTime >= menuController.captionsController.subtitles.get(menuController.captionsController.captionsPosition + 1).timeIn);
                        }
                    }
                });
            }

            @Override
            public void mediaPlayerReady(MediaPlayer mediaPlayer) {

                Platform.runLater(() -> {
                    controlBarController.durationSlider.setMax((double)mediaPlayer.media().info().duration()/1000);
                    if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.slider.setMax((double)mediaPlayer.media().info().duration()/1000);

                    mediaActive.set(true);

                    play();
                });
            }


        });

    }

    public void updateMedia(double newValue) {

        Platform.runLater(() -> {
            if (!controlBarController.showingTimeLeft)
                Utilities.setCurrentTimeLabel(controlBarController.durationLabel, controlBarController.durationSlider, Duration.millis(embeddedMediaPlayer.media().info().duration()));
            else
                Utilities.setTimeLeftLabel(controlBarController.durationLabel, controlBarController.durationSlider, Duration.millis(embeddedMediaPlayer.media().info().duration() ));
        });


        if (atEnd) {
            atEnd = false;
            seekedToEnd = false;

            if (wasPlaying && (!controlBarController.durationSlider.isValueChanging() && (!mainController.miniplayerActive || !mainController.miniplayer.miniplayerController.slider.isValueChanging()))) {

                play();

            } else {

                pause();

            }
        }



        // this final block will probably have to be modified
        else if (newValue >= controlBarController.durationSlider.getMax()) {

            if (controlBarController.durationSlider.isValueChanging() || (mainController.miniplayerActive && mainController.miniplayer.miniplayerController.slider.isValueChanging())) {
                seekedToEnd = true;
            }

            atEnd = true;
            playing.set(false);

            if (!controlBarController.durationSlider.isValueChanging() && (!mainController.miniplayerActive || !mainController.miniplayer.miniplayerController.slider.isValueChanging())) {
                Platform.runLater(this::endMedia);
            }
        }

        if(Math.abs(currentTime/1000 - newValue) > 0.5 || (!playing.get() && Math.abs(currentTime/1000 - newValue) >= 0.1)) {
            currentTime = newValue;
            seek(Duration.seconds(newValue));
        }
        else if(newValue == 0){
            currentTime = newValue;
            seek(Duration.ZERO);
        }



    }

    public void endMedia() {


        if ((!settingsController.playbackOptionsController.shuffleOn && !settingsController.playbackOptionsController.loopOn && !settingsController.playbackOptionsController.autoplayOn) || (settingsController.playbackOptionsController.loopOn && seekedToEnd)) {
            defaultEnd();

        } else if (settingsController.playbackOptionsController.loopOn) {
            controlBarController.mouseEventTracker.move();

            // restart current video
            embeddedMediaPlayer.controls().start();

        }
        else {
            if((menuController.historyBox.index == -1 || menuController.historyBox.index >= menuController.history.size() -1) && menuController.queue.isEmpty()) defaultEnd();
            else requestNext();

        }

    }

    public void createMedia(MenuObject menuObject) {

        MediaItem mediaItem = menuObject.getMediaItem();

        // resets all media state variables before creating a new player
        atEnd = false;
        seekedToEnd = false;
        playing.set(false);
        wasPlaying = false;
        currentTime = 0;



        if(mediaItem.getSubtitles() != null){
            settingsController.captionsController.loadCaptions(mediaItem.getSubtitles(), mediaItem.getSubtitlesOn());
        }
        else if(settingsController.captionsController.captionsSelected){
            mediaItem.setSubtitles(settingsController.captionsController.captionsFile);
            if(menuController.captionsController.captionsOn.get()) mediaItem.setSubtitlesOn(true);
            if(menuController.activeItem != null && !menuController.activeItem.subTextWrapper.getChildren().contains(menuController.activeItem.captionsPane)) menuController.activeItem.subTextWrapper.getChildren().add(0, menuController.activeItem.captionsPane);
        }

        if(mediaItem.getTitle() == null){
            mainController.videoTitleLabel.setText(mediaItem.getFile().getName());
        }
        else {
            mainController.videoTitleLabel.setText(mediaItem.getTitle());
        }

        controlBarController.durationSlider.setValue(0);



        if((menuController.historyBox.index == -1  || menuController.historyBox.index == menuController.history.size() -1) && menuController.queue.isEmpty() && controlBarController.nextVideoButtonEnabled){
            controlBarController.disableNextVideoButton();
        }
        else if(menuController.historyBox.index != -1 && menuController.historyBox.index < menuController.history.size() -1 && !controlBarController.nextVideoButtonEnabled){
            controlBarController.enableNextVideoButton();
        }


        if((menuController.history.isEmpty() || menuController.historyBox.index == 0) && controlBarController.durationSlider.getValue() <= 5 && controlBarController.previousVideoButtonEnabled){
            controlBarController.disablePreviousVideoButton();
        }
        else if(!menuController.history.isEmpty() && (menuController.historyBox.index == -1 || menuController.historyBox.index > 0) && !controlBarController.previousVideoButtonEnabled){
            controlBarController.enablePreviousVideoButton();
        }


        embeddedMediaPlayer.media().start(mediaItem.getFile().getAbsolutePath());
        embeddedMediaPlayer.audio().setVolume((int) controlBarController.volumeSlider.getValue());

    }



    public void resetMediaPlayer(){

        mainController.videoImageView.setImage(null);
        if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.videoImageView.setImage(null);

        controlBarController.durationSlider.setValue(0);

        embeddedMediaPlayer.controls().stop();


        if(controlBarController.showingTimeLeft) controlBarController.durationLabel.setText("−00:00/00:00");
        else controlBarController.durationLabel.setText("00:00/00:00");

        mainController.videoTitleLabel.setText(null);

        if(settingsController.playbackOptionsController.loopOn) settingsController.playbackOptionsController.loopTab.toggle.fire();

        mediaActive.set(false);

    }


    public void requestNext(){
        // called when current video reaches the end
        // if animationsInProgress list is empty, play next video, otherwise start a 1 second timer, at the end of which
        // check again if any animations are in progress, if there are, just end the video.
        // stop timer if user changes video while pausetransition is playing




        if(menuController.animationsInProgress.isEmpty()){
            playNext();
        }
        else {
            transitionTimer = new PauseTransition(Duration.millis(1000));
            transitionTimer.setOnFinished((e) -> {
                if(menuController.animationsInProgress.isEmpty()) playNext();
                else defaultEnd();
            });

            transitionTimer.playFromStart();
        }
    }

    public void playNext(){

        controlBarController.mouseEventTracker.move();

        if(menuController.historyBox.index != -1 && menuController.historyBox.index < menuController.history.size() -1){
            // play next video inside history
            HistoryItem historyItem =  menuController.history.get(menuController.historyBox.index + 1);
            historyItem.play();

        }
        else if((menuController.historyBox.index == menuController.history.size() -1 || menuController.historyBox.index == -1) && !menuController.queue.isEmpty()) {
            // play first item in queue

            QueueItem queueItem = menuController.queue.get(0);
            queueItem.play(true);

        }
    }

    public void playPrevious(){

        controlBarController.mouseEventTracker.move();

        if(!menuController.history.isEmpty() && menuController.historyBox.index == -1){
            // play most recent item in history
            HistoryItem historyItem = menuController.history.get(menuController.history.size() -1);
            historyItem.play();
        }
        else if(menuController.historyBox.index > 0){
            // play previous item
            HistoryItem historyItem = menuController.history.get(menuController.historyBox.index -1);
            historyItem.play();
        }
    }

    public void defaultEnd(){
        controlBarController.durationSlider.setValue(controlBarController.durationSlider.getMax());

        //controlBarController.durationLabel.textProperty().unbind(); // probably not necessary
        controlBarController.durationLabel.setText(Utilities.getTime(new Duration(controlBarController.durationSlider.getMax() * 1000)) + "/" + Utilities.getTime(new Duration(controlBarController.durationSlider.getMax() * 1000)));

        controlBarController.mouseEventTracker.move();

        // add logic to update all the play icons
        controlBarController.end();
        if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.end();
        if(menuController.activeItem != null)menuController.activeItem.updateIconToPlay();

        playing.set(false);


    }

    public void play() {

        if(!mediaActive.get()) return;

        playing.set(true);


        embeddedMediaPlayer.controls().play();

        Platform.runLater(() -> {


            if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.play();

            controlBarController.play();

            if(menuController.activeItem != null) menuController.activeItem.updateIconToPause();

            wasPlaying = playing.get();
        });

    }

    public void pause(){

        if(!mediaActive.get()) return;

        if(playing.get()) {
            playing.set(false);
            embeddedMediaPlayer.controls().pause();
        }

        Platform.runLater(() ->{

            if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.pause();

            controlBarController.pause();

            if(menuController.activeItem != null)menuController.activeItem.updateIconToPlay();
        });

    }

    public void replay(){

        Platform.runLater(() -> {
            controlBarController.durationSlider.setValue(0);
        });

        seek(Duration.ZERO);
        play();



        atEnd = false;
        seekedToEnd = false;
    }


    public void seek(Duration time){
        embeddedMediaPlayer.controls().setTime((long) time.toMillis());
    }

    public void changeVolume(double value){
        embeddedMediaPlayer.audio().setVolume((int) value);
    }

    public void changePlaybackSpeed(double value){
        embeddedMediaPlayer.controls().setRate((float) value);
    }

    public Duration getCurrentTime(){
        return Duration.millis(currentTime);
    }
}
