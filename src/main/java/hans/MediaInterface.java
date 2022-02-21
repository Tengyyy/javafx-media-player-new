package hans;


import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MediaInterface {

    MainController mainController;
    ControlBarController controlBarController;
    SettingsController settingsController;

    // all videos that have been added to the queue or directly to the player
    List<Media> videoList = new ArrayList<Media>();

    // videoList minus the videos that have already been played
    List<Media> unplayedVideoList = new ArrayList<Media>();

    // contains all the videos that have been played, in the order that they were played (necessary to navigate videos with the control arrows)
    List<Media> playedVideoList = new ArrayList<Media>();



    Media currentVideo;
    MediaPlayer mediaPlayer;

    // Variables to keep track of mediaplayer status:
    boolean playing = false; // is mediaplayer currently playing
    boolean wasPlaying = false; // was mediaplayer playing before a seeking action occurred
    public boolean atEnd = false; // is mediaplayer at the end of the video
    public boolean seekedToEnd = false; // true = video was seeked to the end; false = video naturally reached the end or the video is still playing
    ////////////////////////////////////////////////

    MediaInterface(MainController mainController, ControlBarController controlBarController, SettingsController settingsController){
        this.mainController = mainController;
        this.controlBarController = controlBarController;
        this.settingsController = settingsController;
    }

    public void updateMedia(double newValue) {
        if (!controlBarController.showingTimeLeft)
            Utilities.setCurrentTimeLabel(controlBarController.durationLabel, mediaPlayer, currentVideo);
        else
            Utilities.setTimeLeftLabel(controlBarController.durationLabel, mediaPlayer, currentVideo);

        if (atEnd) {
            atEnd = false;
            seekedToEnd = false;

            if (wasPlaying) {
                if (!controlBarController.durationSlider.isValueChanging()) {
                    controlBarController.playLogo.setImage(controlBarController.pauseImage);

                    playing = true;
                    mediaPlayer.play();

                    if (controlBarController.play.isShowing() || controlBarController.replay.isShowing()) {
                        controlBarController.play.hide();
                        controlBarController.replay.hide();
                        controlBarController.pause = new ControlTooltip("Pause (k)", controlBarController.playButton, false, controlBarController.controlBar, 0);
                        controlBarController.pause.showTooltip();
                    } else {
                        controlBarController.pause = new ControlTooltip("Pause (k)", controlBarController.playButton, false, controlBarController.controlBar, 0);
                    }
                }
            } else {
                controlBarController.playLogo.setImage(controlBarController.playImage);
                playing = false;

                if (controlBarController.pause.isShowing() || controlBarController.replay.isShowing()) {
                    controlBarController.pause.hide();
                    controlBarController.replay.hide();
                    controlBarController.play = new ControlTooltip("Play (k)", controlBarController.playButton, false, controlBarController.controlBar, 0);
                    controlBarController.play.showTooltip();
                } else {
                    controlBarController.play = new ControlTooltip("Play (k)", controlBarController.playButton, false, controlBarController.controlBar, 0);
                }

            }
            controlBarController.playButton.setOnAction((e) -> {
                controlBarController.playButtonClick1();
            });
        } else if (newValue >= controlBarController.durationSlider.getMax()) {

            if (controlBarController.durationSlider.isValueChanging()) {
                seekedToEnd = true;
            }

            atEnd = true;
            playing = false;
            mediaPlayer.pause();
            if (!controlBarController.durationSlider.isValueChanging()) {

                endMedia();

            }
        }

        if (Math.abs(mediaPlayer.getCurrentTime().toSeconds() - newValue) > 0.5) {
            mediaPlayer.seek(Duration.seconds(newValue));
        }

        controlBarController.durationTrack.setProgress(controlBarController.durationSlider.getValue() / controlBarController.durationSlider.getMax());


    }

    public void endMedia() {

        if ((!settingsController.shuffleOn && !settingsController.loopOn && !settingsController.autoplayOn) || (settingsController.loopOn && seekedToEnd)) {
            controlBarController.durationSlider.setValue(controlBarController.durationSlider.getMax());

            controlBarController.durationLabel.textProperty().unbind();
            controlBarController.durationLabel.setText(Utilities.getTime(new Duration(controlBarController.durationSlider.getMax() * 1000)) + "/" + Utilities.getTime(currentVideo.getDuration()));


            controlBarController.playLogo.setImage(new Image(controlBarController.replayFile.toURI().toString()));

            if (controlBarController.play.isShowing() || controlBarController.pause.isShowing()) {
                controlBarController.play.hide();
                controlBarController.pause.hide();
                controlBarController.replay = new ControlTooltip("Replay (k)", controlBarController.playButton, false, controlBarController.controlBar, 0);
                controlBarController.replay.showTooltip();
            } else {
                controlBarController.replay = new ControlTooltip("Replay (k)", controlBarController.playButton, false, controlBarController.controlBar, 0);
            }

            controlBarController.playButton.setOnAction((e) -> controlBarController.playButtonClick2());

            if (!controlBarController.controlBarOpen) {
                controlBarController.displayControls();
            }


        } else if (settingsController.loopOn && !seekedToEnd) {
            // restart current video


            mediaPlayer.stop();

        } else if (settingsController.shuffleOn) {

            //if(!controlBarController.controlBarOpen) {
            //	controlBarController.displayControls();
            //}

        } else if (settingsController.autoplayOn) {
            // play next song in queue/directory

            //if(!controlBarController.controlBarOpen) {
            //	controlBarController.displayControls();
            //}
        }

    }

    public void createMediaPlayer(Media media) {

        this.currentVideo = media;

        controlBarController.durationSlider.setValue(0);

        if (unplayedVideoList.contains(media)) unplayedVideoList.remove(media);

        mediaPlayer = new MediaPlayer(media);

        mainController.mediaView.setMediaPlayer(mediaPlayer);

        // update video name field in settings pane and the stage title with the new video
        Platform.runLater(() -> {
            File videoTitleFile = new File(media.getSource().replaceAll("%20", " "));
            settingsController.videoNameText.setText(videoTitleFile.getName()); // updates video name text in settings pane and window title with filename
            App.stage.setTitle(videoTitleFile.getName());
        });

        mediaPlayer.currentTimeProperty().addListener((observableValue, oldTime, newTime) -> {
            if (!controlBarController.showingTimeLeft)
                Utilities.setCurrentTimeLabel(controlBarController.durationLabel, mediaPlayer, media);
            else
                Utilities.setTimeLeftLabel(controlBarController.durationLabel, mediaPlayer, media);

            if (!controlBarController.durationSlider.isValueChanging()) {
                controlBarController.durationSlider.setValue(newTime.toSeconds());
            }

        });


        mediaPlayer.setOnReady(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                mediaPlayer.setVolume(/*controlBarController.volumeSlider.getValue() / 100*/ 0);

                controlBarController.play();

                controlBarController.durationSlider.setMax(Math.floor(media.getDuration().toSeconds()));

                TimerTask setRate = new TimerTask() {

                    @Override
                    public void run() {
                        if(settingsController.playbackSpeedTracker == 0) mediaPlayer.setRate(settingsController.formattedValue);
                        else mediaPlayer.setRate(settingsController.playbackSpeedTracker / 4);
                    }
                };

                Timer timer = new Timer();

                timer.schedule(setRate, 200);
            }

        });

    }

    public void resetMediaPlayer(){
        mediaPlayer.dispose();
        atEnd = false;
        seekedToEnd = false;
        playing = false;
        wasPlaying = false;

        settingsController.videoNameText.setLayoutX(0);

        App.stage.setTitle("MP4 Player");
        settingsController.videoNameText.setText("Choose video");
    }

    public void addVideo(Media media){

    }

    public void addUnplayedVideo(Media media){

    }

    public void addPlayedVideo(Media Media){

    }

    public void removeVideo(Media media){
    }

    public void removePlayedVideo(Media media){

    }

    public void removeUnplayedVideo(Media media){

    }



}
