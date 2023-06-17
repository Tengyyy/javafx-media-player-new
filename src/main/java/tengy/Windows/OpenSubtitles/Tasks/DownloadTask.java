package tengy.Windows.OpenSubtitles.Tasks;

import javafx.concurrent.Task;
import tengy.OpenSubtitles.OpenSubtitles;
import tengy.OpenSubtitles.models.download.DownloadLinkResult;
import tengy.OpenSubtitles.models.features.Subtitle;
import tengy.Utilities;

import java.io.File;
import java.io.IOException;

public class DownloadTask extends Task<File> {

    File parentFile;
    String fileName;

    OpenSubtitles os;

    Subtitle sub;

    public DownloadTask(OpenSubtitles os, File parentFile, String fileName, Subtitle sub){
        this.parentFile = parentFile;
        this.fileName = fileName;
        this.sub = sub;
        this.os = os;
    }


    @Override
    public File call() {

        try {
            if(sub.attributes.files.length > 0){
                Subtitle.FileObject fileObject = sub.attributes.files[0];
                DownloadLinkResult downloadLinkResult = os.getDownloadLink(fileObject);

                File file = Utilities.findFreeFileName(parentFile, fileName);
                os.download(downloadLinkResult, file.toPath());

                return file;
            }
        }
        catch (IOException | InterruptedException ignored){}
        return null;
    }
}
