package hans.Captions.Tasks;

import com.github.wtekiela.opensub4j.response.ListResponse;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import hans.Captions.CaptionsController;
import hans.Captions.OpenSubtitlesPane;
import hans.Captions.Result;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchTask extends Task<List<SubtitleInfo>> {


    CaptionsController captionsController;
    OpenSubtitlesPane openSubtitlesPane;


    public SearchTask(CaptionsController captionsController, OpenSubtitlesPane openSubtitlesPane){
        this.captionsController = captionsController;
        this.openSubtitlesPane = openSubtitlesPane;
    }


    @Override
    protected List<SubtitleInfo> call() {
        ObservableList<Integer> observableList = openSubtitlesPane.languageBox.getCheckModel().getCheckedIndices();
        StringBuilder languageString = new StringBuilder();
        if(observableList.isEmpty()) languageString.append("all");
        else {
            for (int i = 0; i < observableList.size(); i++) {
                Integer index = observableList.get(i);
                String languageName = openSubtitlesPane.languageBox.getItems().get(index);
                String languageCode = openSubtitlesPane.languageMap.get(languageName);
                if (i < observableList.size() - 1) {
                    languageString.append(languageCode).append(", ");
                } else {
                    languageString.append(languageCode);
                }
            }
        }

        try {
            ListResponse<SubtitleInfo> response;
            if(openSubtitlesPane.searchState == 0) response = openSubtitlesPane.osClient.searchSubtitles(languageString.toString(), openSubtitlesPane.titleField.getText(), openSubtitlesPane.seasonField.getText(), openSubtitlesPane.episodeField.getText());
            else if(openSubtitlesPane.searchState == 1) response = openSubtitlesPane.osClient.searchSubtitles(languageString.toString(), openSubtitlesPane.imdbField.getText());
            else response = openSubtitlesPane.osClient.searchSubtitles(languageString.toString(), captionsController.menuController.queueBox.activeItem.get().file);

            if(response.getData().isPresent()){
                return response.getData().get();
            }
            else return new ArrayList<>();

        } catch (XmlRpcException | IOException e) {
            return new ArrayList<>();
        }
    }
}
