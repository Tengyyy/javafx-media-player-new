module hans {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.base;
    requires MaterialFX;
    requires com.jfoenix;
    requires org.apache.logging.log4j;
    requires FX.BorderlessScene;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.javafx;
    requires com.sun.jna.platform;
    requires java.logging;
    requires jaffree;
    requires org.bytedeco.javacv;
    requires org.bytedeco.javacpp;
    requires org.bytedeco.ffmpeg;
    requires opensub4j;
    requires xmlrpc.common;
    requires com.sandec.mdfx;
    requires java.prefs;


    opens tengy to javafx.graphics, javafx.fxml;
    exports tengy;
    exports tengy.MediaItems;
    opens tengy.MediaItems to javafx.fxml, javafx.graphics;
    exports tengy.PlaybackSettings;
    opens tengy.PlaybackSettings to javafx.fxml, javafx.graphics;
    exports tengy.Menu;
    opens tengy.Menu to javafx.fxml, javafx.graphics;
    exports tengy.Menu.MediaInformation;
    opens tengy.Menu.MediaInformation to javafx.fxml, javafx.graphics;
    exports tengy.Subtitles;
    opens tengy.Subtitles to javafx.fxml, javafx.graphics;
    exports tengy.windowstoolbar;
    exports tengy.Chapters;
    opens tengy.Chapters to javafx.fxml, javafx.graphics;

    exports tengy.Menu.Queue;
    opens tengy.Menu.Queue to javafx.fxml, javafx.graphics;
    exports tengy.Menu.Settings;
    opens tengy.Menu.Settings to javafx.fxml, javafx.graphics;
    exports tengy.SRTParser.srt;
    exports tengy.Windows;
    opens tengy.Windows to javafx.fxml, javafx.graphics;
    exports tengy.Windows.ChapterEdit;
    opens tengy.Windows.ChapterEdit to javafx.fxml, javafx.graphics;
    exports tengy.Menu.Settings.Libraries;
    opens tengy.Menu.Settings.Libraries to javafx.fxml, javafx.graphics;
    exports tengy.Windows.Equalizer;
    opens tengy.Windows.Equalizer to javafx.fxml, javafx.graphics;
    exports tengy.Windows.OpenSubtitles;
    opens tengy.Windows.OpenSubtitles to javafx.fxml, javafx.graphics;
}