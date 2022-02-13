package hans;

import java.io.File;
import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Utilities {


    // Create neatly formatted video duration string
    public static String getTime(Duration time) {

        int hours = (int) time.toHours();
        int minutes = (int) time.toMinutes();
        int seconds = (int) time.toSeconds();


        if (seconds > 59)
            seconds = seconds % 60;
        if (minutes > 59)
            minutes = minutes % 60;
        if (hours > 59)
            hours = hours % 60;

        // Don't show the hours unless the video has been playing for an hour or longer.
        if (hours > 0)
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }


    public static void setCurrentTimeLabel(Label durationLabel, MediaPlayer mediaPlayer, Media media) {
        durationLabel.setText(getTime(mediaPlayer.getCurrentTime()) + "/" + getTime(media.getDuration()));
    }

    public static void setTimeLeftLabel(Label durationLabel, MediaPlayer mediaPlayer, Media media) {
        durationLabel.setText("−" + getTime(media.getDuration().subtract(mediaPlayer.getCurrentTime())) + "/" + getTime(media.getDuration()));
    }


    // makes HBox's background lighter and less transparent on hovering
    public static void hoverEffectOn(HBox setting) {
        setting.setStyle("-fx-background-color: rgba(73,73,73,0.8)");
    }

    // turns HBox background back to normal
    public static void hoverEffectOff(HBox setting) {
        setting.setStyle("-fx-background-color: rgba(83,83,83,0)");
    }


    // gets file extension
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int extensionIndex = fileName.lastIndexOf('.');

        if (extensionIndex > 0) {
            return fileName.substring(extensionIndex + 1);
        } else {
            return " ";
        }

    }

    public static void clipChildren(Region region) {
        final Rectangle clipPane = new Rectangle();
        region.setClip(clipPane);

        region.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            clipPane.setWidth(newValue.getWidth());
            clipPane.setHeight(newValue.getHeight());
            System.out.println("su ema");
        });
    }

}
