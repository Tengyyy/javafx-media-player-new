package hans.MediaItems;

import hans.MainController;
import hans.MediaItems.MediaItem;
import hans.Utilities;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameGrabber;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FlacItem implements MediaItem {

    double frameRate = 30;
    float frameDuration = (float) (1 / frameRate);

    File file;
    File subtitles;
    boolean subtitlesOn = false;
    Color backgroundColor = null;
    Duration duration;

    Image cover;

    MainController mainController;

    Map<String, String> mediaInformation = new HashMap<>();
    Map<String, String> mediaDetails = new HashMap<>();


    public FlacItem(File file, MainController mainController) {
        this.file = file;
        this.mainController = mainController;

        try {
            FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(file);


            fFmpegFrameGrabber.start();


            duration = Duration.seconds((int) (fFmpegFrameGrabber.getLengthInAudioFrames() / fFmpegFrameGrabber.getAudioFrameRate()));
            frameRate = fFmpegFrameGrabber.getAudioFrameRate();
            frameDuration = (float) (1 / frameRate);

            for(Map.Entry<String, String> entry : fFmpegFrameGrabber.getMetadata().entrySet()){
                mediaInformation.put(entry.getKey().toLowerCase(), entry.getValue());
            }

            System.out.println("Video codec name: " + fFmpegFrameGrabber.getVideoCodecName());
            System.out.println("Video bitrate: " + fFmpegFrameGrabber.getVideoBitrate());
            System.out.println("Video codec: " + fFmpegFrameGrabber.getVideoCodec());
            System.out.println("Streams: " + fFmpegFrameGrabber.getVideoStream());
            System.out.println("Video framerate: " + fFmpegFrameGrabber.getFrameRate());
            System.out.println("Sample rate: " + fFmpegFrameGrabber.getSampleRate());
            System.out.println("Format: " + fFmpegFrameGrabber.getFormat());
            System.out.println("Aspect ratio: " + fFmpegFrameGrabber.getAspectRatio());
            System.out.println("Image height: " + fFmpegFrameGrabber.getImageHeight());
            System.out.println("Image width: " + fFmpegFrameGrabber.getImageWidth());
            System.out.println("Length in frames: " + fFmpegFrameGrabber.getLengthInFrames());
            System.out.println("Audio framerate: " + fFmpegFrameGrabber.getAudioFrameRate());
            System.out.println("Audio bitrate: " + fFmpegFrameGrabber.getAudioBitrate());
            System.out.println("Audio codec name: " + fFmpegFrameGrabber.getAudioCodecName());
            System.out.println("Audio codec: " + fFmpegFrameGrabber.getAudioCodecName());
            System.out.println("Audio channels: " + fFmpegFrameGrabber.getAudioChannels());

            mediaDetails.put("size", Utilities.formatFileSize(file.length()));
            mediaDetails.put("name", file.getName());
            mediaDetails.put("path", file.getAbsolutePath());
            mediaDetails.put("modified", DateFormat.getDateInstance().format(new Date(file.lastModified())));


            fFmpegFrameGrabber.stop();
            fFmpegFrameGrabber.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(cover == null){
            cover = new Image(Objects.requireNonNull(Objects.requireNonNull(mainController.getClass().getResource("images/default.png")).toExternalForm()));
            backgroundColor = Color.rgb(254, 200, 149);
        }
    }

    @Override
    public float getFrameDuration() {
        return frameDuration;
    }

    @Override
    public Map<String, String> getMediaInformation() {
        return mediaInformation;
    }

    @Override
    public Map<String, String> getMediaDetails() {
        return mediaDetails;
    }


    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public File getSubtitles() {
        return subtitles;
    }

    @Override
    public boolean getSubtitlesOn() {
        return subtitlesOn;
    }

    @Override
    public void setSubtitlesOn(boolean value) {
        subtitlesOn = value;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public Image getCover() {
        return cover;
    }

    @Override
    public void setSubtitles(File file) {
        this.subtitles = file;
    }

    @Override
    public Color getCoverBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setCoverBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    @Override
    public boolean hasVideo() {
        return false;
    }
}