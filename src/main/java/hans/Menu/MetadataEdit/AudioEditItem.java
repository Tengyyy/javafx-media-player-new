package hans.Menu.MetadataEdit;

import hans.MediaItems.AudioItem;
import hans.Menu.MenuObject;
import hans.Utilities;
import javafx.scene.layout.VBox;
import org.jaudiotagger.tag.FieldKey;

import java.time.DateTimeException;
import java.util.*;

public class AudioEditItem implements MetadataEditItem{

    MetadataEditPage metadataEditPage;
    AudioItem audioItem;

    TextAreaItem titleItem = null;
    TextAreaItem artistItem = null;
    TextAreaItem albumItem = null;
    DatePickerItem releaseDateItem = null;
    TwoSpinnerItem trackItem = null;
    TwoSpinnerItem discItem = null;
    TextAreaItem albumArtistItem = null;
    TextAreaItem composerItem = null;
    TextAreaItem publisherItem = null;
    TextAreaItem genreItem = null;
    TextAreaItem languageItem = null;
    TextAreaItem lyricsItem = null;
    TextAreaItem commentItem = null;


    VBox content = new VBox();

    Map<String, String> metadata;


    AudioEditItem(MetadataEditPage metadataEditPage, AudioItem audioItem){
        this.metadataEditPage = metadataEditPage;
        this.audioItem = audioItem;

        content.setSpacing(15);

        metadata = audioItem.getMediaInformation();

        if(metadata != null) {
            titleItem = new TextAreaItem(metadataEditPage, "Title", metadata.get("title").isBlank() ? "" : metadata.get("title"), content, true);
            artistItem = new TextAreaItem(metadataEditPage, "Artist", metadata.get("artist").isBlank() ? "" : metadata.get("artist"), content, true);
            albumItem = new TextAreaItem(metadataEditPage, "Album", metadata.get("album").isBlank() ? "" : metadata.get("album"), content, true);
            releaseDateItem = new DatePickerItem(metadataEditPage, metadata.get("year").isBlank() ? "" : metadata.get("year"), content, true);
            trackItem = new TwoSpinnerItem(metadataEditPage, "Track number", metadata.get("track"), metadata.get("track_total"), content, true); // keep an eye on potential problems for the twofield items
            discItem = new TwoSpinnerItem(metadataEditPage, "Disc number", metadata.get("disc_no"), metadata.get("disc_total"), content, true);
            albumArtistItem = new TextAreaItem(metadataEditPage, "Album artist", metadata.get("album_artist").isBlank() ? "" : metadata.get("album_artist"), content, true);
            composerItem = new TextAreaItem(metadataEditPage, "Composer", metadata.get("composer").isBlank() ? "" : metadata.get("composer"), content, true);
            publisherItem = new TextAreaItem(metadataEditPage, "Publisher", metadata.get("record_label").isBlank() ? "" : metadata.get("record_label"), content, true);
            genreItem = new TextAreaItem(metadataEditPage, "Genre", metadata.get("genre").isBlank() ? "" : metadata.get("genre"), content, true);
            languageItem = new TextAreaItem(metadataEditPage, "Language", metadata.get("language").isBlank() ? "" : metadata.get("language"), content, true);
            lyricsItem = new TextAreaItem(metadataEditPage, "Lyrics", metadata.get("lyrics").isBlank() ? "" : metadata.get("lyrics"), content, true);
            commentItem = new TextAreaItem(metadataEditPage, "Comment", metadata.get("comment").isBlank() ? "" : metadata.get("comment"), content, true);
        }

        metadataEditPage.textBox.getChildren().add(content);
    }

    @Override
    public Map<String, String> saveMetadata(){
        Map<String, String> mediaInformation = new HashMap<>();

        mediaInformation.put("title", titleItem.textArea.getText());
        mediaInformation.put("artist", artistItem.textArea.getText());
        mediaInformation.put("album", albumItem.textArea.getText());
        if(releaseDateItem.datePicker.getValue() != null) mediaInformation.put("year", releaseDateItem.datePicker.getValue().format(releaseDateItem.dateTimeFormatter));
        else mediaInformation.put("year", "");
        mediaInformation.put("track", String.valueOf(trackItem.numberSpinner1.spinner.getValue()));
        mediaInformation.put("track_total", String.valueOf(trackItem.numberSpinner2.spinner.getValue()));
        mediaInformation.put("disc_no", String.valueOf(discItem.numberSpinner1.spinner.getValue()));
        mediaInformation.put("disc_total", String.valueOf(discItem.numberSpinner2.spinner.getValue()));
        mediaInformation.put("album_artist", albumArtistItem.textArea.getText());
        mediaInformation.put("composer", composerItem.textArea.getText());
        mediaInformation.put("record_label", publisherItem.textArea.getText());
        mediaInformation.put("genre", genreItem.textArea.getText());
        mediaInformation.put("language", languageItem.textArea.getText());
        mediaInformation.put("lyrics", lyricsItem.textArea.getText());
        mediaInformation.put("comment", commentItem.textArea.getText());

        return mediaInformation;
    }
}

