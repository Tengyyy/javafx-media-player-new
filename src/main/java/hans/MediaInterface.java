package hans;


import hans.Subtitles.SubtitlesController;
import hans.Subtitles.SubtitlesState;
import hans.Chapters.ChapterController;
import hans.Chapters.ChapterFrameGrabberTask;
import hans.Chapters.ChapterItem;
import hans.MediaItems.MediaItem;
import hans.Menu.MenuController;
import hans.Menu.Queue.QueueItem;
import hans.PlaybackSettings.PlaybackSettingsController;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.Equalizer;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.bytedeco.ffmpeg.global.avformat.AV_DISPOSITION_DEFAULT;


public class MediaInterface {

    MainController mainController;
    ControlBarController controlBarController;
    PlaybackSettingsController playbackSettingsController;
    MenuController menuController;
    SubtitlesController subtitlesController;
    ChapterController chapterController;

    public MediaPlayerFactory mediaPlayerFactory;

    public EmbeddedMediaPlayer embeddedMediaPlayer;

    // Variables to keep track of mediaplayer status:
    public BooleanProperty mediaActive = new SimpleBooleanProperty(false); // is the mediaplayer active (is any video currently loaded in)
    public BooleanProperty playing = new SimpleBooleanProperty(false); // is mediaplayer currently playing
    public boolean wasPlaying = false; // was mediaplayer playing before a seeking action occurred
    public boolean atEnd = false; // is mediaplayer at the end of the video
    public boolean seekedToEnd = false; // true = video was seeked to the end; false = video naturally reached the end or the video is still playing
    ////////////////////////////////////////////////



    double currentTime;

    public FFmpegFrameGrabber fFmpegFrameGrabber;

    FrameGrabberTask frameGrabberTask;

    public SubtitleExtractionTask subtitleExtractionTask;
    public ExecutorService executorService;

    ImageViewVideoSurface imageViewVideoSurface = null;

    MediaInterface(MainController mainController, ControlBarController controlBarController, PlaybackSettingsController playbackSettingsController, MenuController menuController, SubtitlesController subtitlesController) {
        this.mainController = mainController;
        this.controlBarController = controlBarController;
        this.playbackSettingsController = playbackSettingsController;
        this.menuController = menuController;
        this.subtitlesController = subtitlesController;

        mediaActive.addListener((observableValue, oldValue, newValue) -> {
            controlBarController.durationPane.setMouseTransparent(!newValue);
            if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.sliderPane.setMouseTransparent(!newValue);


            if(newValue){
                if(!controlBarController.playButtonEnabled) controlBarController.enablePlayButton();
                if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.enablePlayButton();
            }
            else {
                if(controlBarController.playButtonEnabled) {
                    controlBarController.pause();
                    controlBarController.disablePlayButton();
                }
                if(mainController.miniplayerActive){
                    mainController.miniplayer.miniplayerController.pause();
                    mainController.miniplayer.miniplayerController.disablePlayButton();
                }
            }
        });
    }

    public void init(ChapterController chapterController){

        this.chapterController = chapterController;

        String[] VLC_GLOBAL_OPTIONS = {
                "--no-sub-autodetect-file",
                "--no-spu",
                "--disable-screensaver"
        };

        this.mediaPlayerFactory = new MediaPlayerFactory(VLC_GLOBAL_OPTIONS);
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        imageViewVideoSurface = new ImageViewVideoSurface(mainController.videoImageView);

        embeddedMediaPlayer.videoSurface().set(imageViewVideoSurface);
        embeddedMediaPlayer.audio().setEqualizer(new Equalizer(10));
        embeddedMediaPlayer.audio().equalizer().setPreamp(0);
        embeddedMediaPlayer.audio().setVolume(50);


        embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> controlBarController.durationSlider.setValue(controlBarController.durationSlider.getMax()));
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                currentTime = newTime;

                Platform.runLater(() -> {

                    mainController.videoImageView.setVisible(true);
                    mainController.seekImageView.setVisible(false);
                    mainController.seekImageView.setImage(null);

                    if(mainController.miniplayerActive){
                        mainController.miniplayer.miniplayerController.videoImageView.setVisible(true);
                        mainController.miniplayer.miniplayerController.seekImageView.setVisible(false);
                        mainController.miniplayer.miniplayerController.seekImageView.setImage(null);
                    }

                    if(!controlBarController.durationSlider.isValueChanging() && !mainController.seekingWithKeys && Math.abs(currentTime/1000 - controlBarController.durationSlider.getValue()) > 0.2 && Math.abs(currentTime/1000 - controlBarController.durationSlider.getValue()) < 2.0 && (!mainController.miniplayerActive || !mainController.miniplayer.miniplayerController.slider.isValueChanging())) controlBarController.durationSlider.setValue((double)newTime/1000);
                });
            }

            @Override
            public void mediaPlayerReady(MediaPlayer mediaPlayer) {

                if(mediaActive.get()) return;

                Image image = null;
                if(mainController.videoImageView.getImage() != null){
                    image = mainController.videoImageView.getImage();
                }
                else if(mainController.miniplayerActive && mainController.miniplayer.miniplayerController.videoImageView.getImage() != null){
                    image = mainController.miniplayer.miniplayerController.videoImageView.getImage();
                }

                if(image != null && menuController.queuePage.queueBox.activeItem.get() != null && menuController.queuePage.queueBox.activeItem.get().getMediaItem() != null){
                    MediaItem mediaItem = menuController.queuePage.queueBox.activeItem.get().getMediaItem();
                    mediaItem.width = image.getWidth();
                    mediaItem.height = image.getHeight();


                    double ratio = image.getWidth()/image.getHeight();

                    int newWidth = (int) Math.min(160, 90 * ratio);
                    int newHeight = (int) Math.min(90, 160/ratio);

                    if(fFmpegFrameGrabber != null){
                        fFmpegFrameGrabber.setImageWidth(newWidth);
                        fFmpegFrameGrabber.setImageHeight(newHeight);
                    }
                }


                Platform.runLater(() -> {

                    mediaActive.set(true);

                    mediaPlayer.audio().setVolume((int) controlBarController.volumeSlider.getValue());
                    controlBarController.durationSlider.setMax((double)mediaPlayer.media().info().duration()/1000);
                    if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.slider.setMax((double)mediaPlayer.media().info().duration()/1000);

                    if(controlBarController.showingTimeLeft) Utilities.setTimeLeftLabel(controlBarController.durationLabel, Duration.ZERO, Duration.seconds(controlBarController.durationSlider.getMax()));
                    else Utilities.setCurrentTimeLabel(controlBarController.durationLabel, Duration.ZERO, Duration.seconds(controlBarController.durationSlider.getMax()));

                    chapterController.initializeChapters(mediaPlayer.chapters().descriptions(), menuController.queuePage.queueBox.activeItem.get().file);

                    if(menuController.queuePage.queueBox.activeItem.get() != null && menuController.queuePage.queueBox.activeItem.get().getMediaItem() != null && menuController.queuePage.queueBox.activeItem.get().getMediaItem().hasVideo() && !menuController.chapterController.chapterPage.chapterBox.getChildren().isEmpty()){
                        ExecutorService executorService = Executors.newFixedThreadPool(1);
                        for(Node node : menuController.chapterController.chapterPage.chapterBox.getChildren()){
                            ChapterItem chapterItem = (ChapterItem) node;
                            Duration startTime = chapterItem.startTime;
                            ChapterFrameGrabberTask chapterFrameGrabberTask;
                            if(startTime.greaterThan(Duration.ZERO)) chapterFrameGrabberTask = new ChapterFrameGrabberTask(fFmpegFrameGrabber, startTime.toSeconds()/menuController.controlBarController.durationSlider.getMax());
                            else {
                                Duration endTime = chapterItem.endTime;
                                chapterFrameGrabberTask = new ChapterFrameGrabberTask(fFmpegFrameGrabber, (Math.min(endTime.toSeconds()/10, 5))/menuController.controlBarController.durationSlider.getMax());
                            }
                            chapterFrameGrabberTask.setOnSucceeded((event) -> chapterItem.coverImage.setImage(chapterFrameGrabberTask.getValue()));

                            executorService.execute(chapterFrameGrabberTask);
                        }
                        executorService.shutdown();
                    }

                    play();
                });
            }
        });

    }

    public void updateMedia(double newValue) {

        if (!controlBarController.showingTimeLeft)
            Utilities.setCurrentTimeLabel(controlBarController.durationLabel, Duration.seconds(controlBarController.durationSlider.getValue()), Duration.seconds(controlBarController.durationSlider.getMax()));
        else
            Utilities.setTimeLeftLabel(controlBarController.durationLabel, Duration.seconds(controlBarController.durationSlider.getValue()), Duration.seconds(controlBarController.durationSlider.getMax()));

        if (newValue >= controlBarController.durationSlider.getMax()) {
            if (controlBarController.durationSlider.isValueChanging() || (mainController.miniplayerActive && mainController.miniplayer.miniplayerController.slider.isValueChanging())) seekedToEnd = true;
            else if(seekedToEnd) defaultEnd();
            else if(playbackSettingsController.playbackOptionsController.loopOn){
                controlBarController.durationSlider.setValue(0);
                return;
            }

            atEnd = true;

            currentTime = controlBarController.durationSlider.getMax();

            playing.set(false);
            embeddedMediaPlayer.controls().setPause(true);
            SleepSuppressor.allowSleep();

            if(!controlBarController.durationSlider.isValueChanging() && (!mainController.miniplayerActive || !mainController.miniplayer.miniplayerController.slider.isValueChanging())){
                seek(Duration.seconds(newValue));

                if(!seekedToEnd && !playbackSettingsController.playbackOptionsController.loopOn) endMedia();
            }

        }
        else {
            if(newValue == 0){
                currentTime = 0;
                if(!controlBarController.durationSlider.isValueChanging() && (!mainController.miniplayerActive || !mainController.miniplayer.miniplayerController.slider.isValueChanging())) seek(Duration.ZERO);
            }
            else if(Math.abs(currentTime/1000 - newValue) > 0.5 || (!playing.get() && Math.abs(currentTime/1000 - newValue) >= 0.1)) {
                currentTime = newValue;
                if(!controlBarController.durationSlider.isValueChanging() && (!mainController.miniplayerActive || !mainController.miniplayer.miniplayerController.slider.isValueChanging())) seek(Duration.seconds(newValue));
            }

            if (atEnd) {
                atEnd = false;
                seekedToEnd = false;

                if (wasPlaying && (!controlBarController.durationSlider.isValueChanging() && (!mainController.miniplayerActive || !mainController.miniplayer.miniplayerController.slider.isValueChanging()))) play();
                else pause();
            }
        }

    }

    public void endMedia() {


        if ((!playbackSettingsController.playbackOptionsController.shuffleOn && !playbackSettingsController.playbackOptionsController.loopOn && !playbackSettingsController.playbackOptionsController.autoplayOn) || (playbackSettingsController.playbackOptionsController.loopOn && seekedToEnd)) {
            defaultEnd();

        } else if (playbackSettingsController.playbackOptionsController.loopOn) {
            controlBarController.mouseEventTracker.move();

            // restart current video
        }
        else {
            if(menuController.queuePage.queueBox.queue.isEmpty() || menuController.queuePage.queueBox.activeIndex.get() >= menuController.queuePage.queueBox.queue.size() - 1) defaultEnd();
            else playNext();

        }

    }

    public void createMedia(QueueItem queueItem) {

        mainController.coverImageContainer.setVisible(false);
        mainController.miniplayerActiveText.setVisible(false);

        subtitlesController.timingPane.resetTiming();

        if(mainController.miniplayerActive){
            mainController.miniplayer.miniplayerController.videoImageView.setImage(null);
            mainController.miniplayer.miniplayerController.coverImageContainer.setVisible(false);
        }

        subtitlesController.openSubtitlesPane.fileSearchLabel.setText("Current media file:\n" + queueItem.file.getName());
        subtitlesController.openSubtitlesPane.fileSearchLabelContainer.setAlignment(Pos.CENTER_LEFT);
        if(!subtitlesController.openSubtitlesPane.fileSearchLabelContainer.getChildren().contains(subtitlesController.openSubtitlesPane.fileSearchExplanationLabel)) subtitlesController.openSubtitlesPane.fileSearchLabelContainer.getChildren().add(subtitlesController.openSubtitlesPane.fileSearchExplanationLabel);

        if(!subtitlesController.openSubtitlesPane.searchInProgress.get()) subtitlesController.openSubtitlesPane.searchButton.setDisable(false);

        MediaItem mediaItem = queueItem.getMediaItem();

        if (mediaItem != null) loadMediaItem(queueItem);

        controlBarController.durationSlider.setValue(0);

        embeddedMediaPlayer.media().start(queueItem.file.getAbsolutePath());
    }


    public void resetMediaPlayer(){

        mainController.videoImageView.setImage(null);
        mainController.videoImageView.setVisible(true);
        if(mainController.miniplayerActive){
            mainController.miniplayer.miniplayerController.videoImageView.setImage(null);
            mainController.miniplayer.miniplayerController.coverImageContainer.setVisible(false);

            mainController.miniplayer.miniplayerController.seekImageView.setImage(null);
            mainController.miniplayer.miniplayerController.seekImageView.setVisible(false);
        }

        mainController.seekImageView.setImage(null);
        mainController.seekImageView.setVisible(false);

        mainController.coverImageContainer.setVisible(false);
        mainController.miniplayerActiveText.setVisible(false);

        mediaActive.set(false);
        chapterController.resetChapters();

        controlBarController.durationSlider.setValue(0);

        embeddedMediaPlayer.controls().stop();
        SleepSuppressor.allowSleep();

        if(controlBarController.showingTimeLeft) controlBarController.durationLabel.setText("−00:00/00:00");
        else controlBarController.durationLabel.setText("00:00/00:00");

        mainController.videoTitleLabel.setText(null);
        mainController.metadataButtonPane.setVisible(false);
        mainController.metadataButtonPane.setMouseTransparent(true);

        if(playbackSettingsController.playbackOptionsController.loopOn) playbackSettingsController.playbackOptionsController.loopTab.toggle.fire();

        mainController.sliderHoverPreview.setImage(null);

        subtitlesController.clearSubtitles();
        subtitlesController.openSubtitlesPane.fileSearchLabel.setText("Select a media file to use this feature");
        subtitlesController.openSubtitlesPane.fileSearchLabelContainer.setAlignment(Pos.CENTER);
        subtitlesController.openSubtitlesPane.fileSearchLabelContainer.getChildren().remove(subtitlesController.openSubtitlesPane.fileSearchExplanationLabel);

        if(subtitlesController.openSubtitlesPane.searchState == 2) subtitlesController.openSubtitlesPane.searchButton.setDisable(true);


        controlBarController.disablePreviousVideoButton();
        controlBarController.disableNextVideoButton();

        atEnd = false;
        seekedToEnd = false;
        playing.set(false);
        wasPlaying = false;
        currentTime = 0;

        subtitlesController.timingPane.resetTiming();

        try {
            if(fFmpegFrameGrabber != null) fFmpegFrameGrabber.close();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    public void playNext(){

        controlBarController.mouseEventTracker.move();

        if(menuController.queuePage.queueBox.activeItem.get() != null && menuController.queuePage.queueBox.queue.size() > menuController.queuePage.queueBox.activeIndex.get() + 1){
            menuController.queuePage.queueBox.queue.get(menuController.queuePage.queueBox.queueOrder.get(menuController.queuePage.queueBox.activeIndex.get() + 1)).play();
        }
        else if(menuController.queuePage.queueBox.activeItem.get() == null && !menuController.queuePage.queueBox.queue.isEmpty()) menuController.queuePage.queueBox.queue.get(menuController.queuePage.queueBox.queueOrder.get(0)).play();
    }

    public void playPrevious(){
        controlBarController.mouseEventTracker.move();

        if(menuController.queuePage.queueBox.activeItem.get() != null && menuController.queuePage.queueBox.activeIndex.get() > 0) menuController.queuePage.queueBox.queue.get(menuController.queuePage.queueBox.queueOrder.get(menuController.queuePage.queueBox.activeIndex.get() -1)).play();
    }

    public void defaultEnd(){
        controlBarController.durationSlider.setValue(controlBarController.durationSlider.getMax());

        if(controlBarController.showingTimeLeft) Utilities.setTimeLeftLabel(controlBarController.durationLabel, Duration.seconds(controlBarController.durationSlider.getMax()), Duration.seconds(controlBarController.durationSlider.getMax()));
        else Utilities.setCurrentTimeLabel(controlBarController.durationLabel, Duration.seconds(controlBarController.durationSlider.getMax()), Duration.seconds(controlBarController.durationSlider.getMax()));

        controlBarController.mouseEventTracker.move();

        controlBarController.end();
        if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.end();
        if(menuController.queuePage.queueBox.activeItem.get() != null) menuController.queuePage.queueBox.activeItem.get().updateIconToPlay();

        playing.set(false);


    }

    public void play() {

        if(!mediaActive.get()) return;

        if(!playing.get()){
            playing.set(true);
            embeddedMediaPlayer.controls().play();
            SleepSuppressor.preventSleep();
        }

        if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.play();

        controlBarController.play();

        if(menuController.queuePage.queueBox.activeItem.get() != null) menuController.queuePage.queueBox.activeItem.get().updateIconToPause();

        wasPlaying = true;

    }

    public void pause(){

        if(!mediaActive.get()) return;

        if(playing.get()) {
            playing.set(false);
            embeddedMediaPlayer.controls().pause();
            SleepSuppressor.allowSleep();
        }


        if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.pause();

        controlBarController.pause();

        if(menuController.queuePage.queueBox.activeItem.get() != null) menuController.queuePage.queueBox.activeItem.get().updateIconToPlay();

    }

    public void replay(){
        controlBarController.durationSlider.setValue(0);
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


    public void updatePreviewFrame(double time, boolean forceUpdate) {

        if(!forceUpdate && (fFmpegFrameGrabber == null || frameGrabberTask != null && frameGrabberTask.isRunning())) return;

        frameGrabberTask = new FrameGrabberTask(fFmpegFrameGrabber, time);

        frameGrabberTask.setOnSucceeded((succeededEvent) -> {
            Image image = frameGrabberTask.getValue();
            if(image == null) return;
            if(mainController.miniplayerActive && mainController.miniplayer.miniplayerController.slider.isValueChanging()){
                mainController.miniplayer.miniplayerController.seekImageView.setImage(image);
            }
            else {
                mainController.sliderHoverPreview.imageView.setImage(image);
                if(controlBarController.durationSlider.isValueChanging()){
                    if(mainController.miniplayerActive) mainController.miniplayer.miniplayerController.seekImageView.setImage(image);
                    else mainController.seekImageView.setImage(image);
                }
            }
        });


        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(frameGrabberTask);
        executorService.shutdown();
    }

    public void loadMediaItem(QueueItem queueItem){
        MediaItem mediaItem = queueItem.getMediaItem();
        if(mediaItem == null) return;

        if(mediaItem.hasVideo()){

            fFmpegFrameGrabber = new FFmpegFrameGrabber(mediaItem.getFile());
            fFmpegFrameGrabber.setVideoDisposition(AV_DISPOSITION_DEFAULT);
            fFmpegFrameGrabber.setVideoOption("vcodec", "copy");

            double width = mediaItem.width;
            double height = mediaItem.height;
            double ratio = width /height;

            int newWidth = (int) Math.min(160, 90 * ratio);
            int newHeight = (int) Math.min(90, 160/ratio);

            fFmpegFrameGrabber.setImageWidth(newWidth);
            fFmpegFrameGrabber.setImageHeight(newHeight);

            try {
                fFmpegFrameGrabber.start();
            } catch (FFmpegFrameGrabber.Exception e) {
                e.printStackTrace();
            }

            if(mainController.miniplayerActive) mainController.miniplayerActiveText.setVisible(true);
        }
        else mainController.setCoverImageView(queueItem);

        mainController.videoTitleLabel.setText(queueItem.getTitle());

        mainController.metadataButton.setOnAction(e -> queueItem.showMetadata());
        mainController.metadataButtonPane.setVisible(true);
        mainController.metadataButtonPane.setMouseTransparent(false);

        if(!mediaItem.subtitlesGenerationTime.isEmpty()){ // caption extraction has started for this mediaitem
            if(!mediaItem.subtitlesExtractionInProgress.get()){ // caption extraction has already been completed, can simply add caption tabs
                subtitlesController.createSubtitleTabs(mediaItem);
            }
            else { // caption extraction is ongoing, have to wait for it to finish before adding caption tabs
                mediaItem.subtitlesExtractionInProgress.addListener((observableValue, oldValue, newValue) -> {
                    if(!newValue && menuController.queuePage.queueBox.activeItem.get() == queueItem) subtitlesController.createSubtitleTabs(mediaItem);
                });
            }
        }
        else { // caption extraction has not started, will create subtitle extraction task and on completion add subtitles
            executorService = Executors.newFixedThreadPool(1);
            subtitleExtractionTask = new SubtitleExtractionTask(subtitlesController, mediaItem);
            subtitleExtractionTask.setOnSucceeded(e -> {
                if(subtitleExtractionTask.getValue() != null && subtitleExtractionTask.getValue() && menuController.queuePage.queueBox.activeItem.get() == queueItem) subtitlesController.createSubtitleTabs(mediaItem);
            });
            executorService.execute(subtitleExtractionTask);
            executorService.shutdown();
        }

        if(mediaItem.hasVideo() && !menuController.chapterController.chapterPage.chapterBox.getChildren().isEmpty()){
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            for(Node node : menuController.chapterController.chapterPage.chapterBox.getChildren()){
                ChapterItem chapterItem = (ChapterItem) node;
                Duration startTime = chapterItem.startTime;
                ChapterFrameGrabberTask chapterFrameGrabberTask;
                if(startTime.greaterThan(Duration.ZERO)) chapterFrameGrabberTask = new ChapterFrameGrabberTask(fFmpegFrameGrabber, startTime.toSeconds()/menuController.controlBarController.durationSlider.getMax());
                else {
                    Duration endTime = chapterItem.endTime;
                    chapterFrameGrabberTask = new ChapterFrameGrabberTask(fFmpegFrameGrabber, (Math.min(endTime.toSeconds()/10, 5))/menuController.controlBarController.durationSlider.getMax());
                }
                chapterFrameGrabberTask.setOnSucceeded((event) -> chapterItem.coverImage.setImage(chapterFrameGrabberTask.getValue()));

                executorService.execute(chapterFrameGrabberTask);
            }
            executorService.shutdown();
        }

        if(subtitlesController.subtitlesState == SubtitlesState.CLOSED){
            Map<String, String> metadata = mediaItem.getMediaInformation();
            if(metadata.containsKey("title") && !metadata.get("title").isBlank()) subtitlesController.openSubtitlesPane.titleField.setText(metadata.get("title"));
            if(metadata.containsKey("season") && !metadata.get("season").isBlank()) subtitlesController.openSubtitlesPane.seasonField.setText(metadata.get("season"));
            if(metadata.containsKey("episode") && !metadata.get("episode").isBlank()) subtitlesController.openSubtitlesPane.episodeField.setText(metadata.get("episode"));
        }

    }
}
