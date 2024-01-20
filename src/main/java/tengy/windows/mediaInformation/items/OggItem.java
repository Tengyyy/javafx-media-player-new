package tengy.windows.mediaInformation.items;

import javafx.scene.layout.VBox;
import tengy.windows.mediaInformation.components.DatePickerItem;
import tengy.windows.mediaInformation.components.DoubleSpinnerItem;
import tengy.windows.mediaInformation.components.TextAreaItem;
import tengy.windows.mediaInformation.components.TextFieldItem;
import tengy.windows.mediaInformation.MediaInformationWindow;

import java.util.HashMap;
import java.util.Map;

public class OggItem implements MediaInformationItem {

    MediaInformationWindow mediaInformationWindow;

    TextFieldItem titleItem = null;
    TextFieldItem artistItem = null;
    TextFieldItem albumItem = null;
    DatePickerItem releaseDateItem = null;
    DoubleSpinnerItem trackItem = null;
    DoubleSpinnerItem discItem = null;
    TextAreaItem albumArtistItem = null;
    TextAreaItem composerItem = null;
    TextAreaItem producerItem = null;
    TextFieldItem publisherItem = null;
    TextFieldItem genreItem = null;
    TextAreaItem lyricsItem = null;
    TextAreaItem commentItem = null;


    VBox content = new VBox();

    Map<String, String> metadata;


    public OggItem(MediaInformationWindow mediaInformationWindow, Map<String, String> metadata){
        this.mediaInformationWindow = mediaInformationWindow;
        this.metadata = metadata;

        content.setSpacing(15);


        if(metadata != null) {
            titleItem = new TextFieldItem(mediaInformationWindow, "Title", metadata.containsKey("TITLE") && !metadata.get("TITLE").isBlank() ? metadata.get("TITLE") : "", content, true);
            artistItem = new TextFieldItem(mediaInformationWindow, "Artist", metadata.containsKey("ARTIST") && !metadata.get("ARTIST").isBlank() ? metadata.get("ARTIST") : "", content, true);
            albumItem = new TextFieldItem(mediaInformationWindow, "Album", metadata.containsKey("ALBUM") && !metadata.get("ALBUM").isBlank() ? metadata.get("ALBUM") : "", content, true);
            releaseDateItem = new DatePickerItem(mediaInformationWindow, metadata.containsKey("DATE") && !metadata.get("DATE").isBlank() ? metadata.get("DATE") : "", content, true);


            trackItem = new DoubleSpinnerItem(mediaInformationWindow, "Track number", metadata.containsKey("track") && !metadata.get("track").isBlank() ? metadata.get("track") : "", metadata.containsKey("TRACKTOTAL") && !metadata.get("TRACKTOTAL").isBlank() ? metadata.get("TRACKTOTAL") : "", content, true);
            discItem = new DoubleSpinnerItem(mediaInformationWindow, "Disc number", metadata.containsKey("disc") && !metadata.get("disc").isBlank() ? metadata.get("disc") : "", metadata.containsKey("DISCTOTAL") && !metadata.get("DISCTOTAL").isBlank() ? metadata.get("DISCTOTAL") : "", content, true);
            albumArtistItem = new TextAreaItem(mediaInformationWindow, "Album artist", metadata.containsKey("album_artist") && !metadata.get("album_artist").isBlank() ? metadata.get("album_artist") : "", content, true);
            composerItem = new TextAreaItem(mediaInformationWindow, "Composer", metadata.containsKey("COMPOSER") && !metadata.get("COMPOSER").isBlank() ? metadata.get("COMPOSER") : "", content, true);
            producerItem = new TextAreaItem(mediaInformationWindow, "Producer",  metadata.containsKey("PRODUCER") && !metadata.get("PRODUCER").isBlank() ? metadata.get("PRODUCER") : "", content, true);
            publisherItem = new TextFieldItem(mediaInformationWindow, "Publisher", metadata.containsKey("PUBLISHER") && !metadata.get("PUBLISHER").isBlank() ? metadata.get("PUBLISHER") : "", content, true);
            genreItem = new TextFieldItem(mediaInformationWindow, "Genre", metadata.containsKey("GENRE") && !metadata.get("GENRE").isBlank() ? metadata.get("GENRE") : "", content, true);
            lyricsItem = new TextAreaItem(mediaInformationWindow, "Lyrics", metadata.containsKey("LYRICS") && !metadata.get("LYRICS").isBlank() ? metadata.get("LYRICS") : "", content, true);
            commentItem = new TextAreaItem(mediaInformationWindow, "Comment", metadata.containsKey("comment") && !metadata.get("comment").isBlank() ? metadata.get("comment") : "", content, true);
        }

        mediaInformationWindow.textBox.getChildren().add(content);
    }

    @Override
    public Map<String, String> createMetadataMap(){
        Map<String, String> mediaInformation = new HashMap<>();

        mediaInformation.put("TITLE", titleItem.textField.getText());
        mediaInformation.put("ARTIST", artistItem.textField.getText());
        mediaInformation.put("ALBUM", albumItem.textField.getText());
        String date = releaseDateItem.parseDate();
        mediaInformation.put("DATE", date);

        mediaInformation.put("track", String.valueOf(trackItem.numberSpinner1.spinner.getValue()));
        mediaInformation.put("TRACKTOTAL", String.valueOf(trackItem.numberSpinner2.spinner.getValue()));
        mediaInformation.put("disc", String.valueOf(discItem.numberSpinner1.spinner.getValue()));
        mediaInformation.put("DISCTOTAL", String.valueOf(discItem.numberSpinner2.spinner.getValue()));
        mediaInformation.put("album_artist", albumArtistItem.textArea.getText());
        mediaInformation.put("COMPOSER", composerItem.textArea.getText());
        mediaInformation.put("PRODUCER", producerItem.textArea.getText());
        mediaInformation.put("PUBLISHER", publisherItem.textField.getText());
        mediaInformation.put("GENRE", genreItem.textField.getText());
        mediaInformation.put("LYRICS", lyricsItem.textArea.getText());
        mediaInformation.put("comment", commentItem.textArea.getText());

        return mediaInformation;
    }
}

