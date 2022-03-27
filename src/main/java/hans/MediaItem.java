package hans;


import javafx.scene.media.Media;

import java.io.File;
import java.util.Map;

public interface MediaItem {


    public  float getFrameDuration();

    public  Map getMediaInformation();

    public  Map getMediaDetails();

    public Media getMedia();

    public File getFile();

}
