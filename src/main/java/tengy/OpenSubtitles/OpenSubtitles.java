package tengy.OpenSubtitles;

import tengy.OpenSubtitles.models.Query;
import tengy.OpenSubtitles.models.authentication.Credentials;
import tengy.OpenSubtitles.models.authentication.LoginResult;
import tengy.OpenSubtitles.models.authentication.LogoutResult;
import tengy.OpenSubtitles.models.discover.DiscoverResult;
import tengy.OpenSubtitles.models.download.DownloadBody;
import tengy.OpenSubtitles.models.download.DownloadLinkResult;
import tengy.OpenSubtitles.models.features.Feature;
import tengy.OpenSubtitles.models.features.FeatureResult;
import tengy.OpenSubtitles.models.features.Subtitle;
import tengy.OpenSubtitles.models.infos.FormatsResult;
import tengy.OpenSubtitles.models.infos.LanguagesResult;
import tengy.OpenSubtitles.models.infos.UserResult;
import tengy.OpenSubtitles.models.subtitles.SubtitlesResult;
import tengy.OpenSubtitles.models.utilities.GuessItQuery;
import tengy.OpenSubtitles.models.utilities.GuessItResult;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.TreeMap;

public class OpenSubtitles {

    private final String key;
    private final Credentials credentials;
    private TreeMap<String,String> header;

    public OpenSubtitles(String username, String password, String apikey) {
        credentials = new Credentials(username,password);
        this.key = apikey;
        header = new TreeMap<>();
        header.put("Accept","application/json");
        header.put("Api-Key", key);
        header.put("Content-Type", "application/json");
        //header.put("Accept-Language","en-US,en;q=0.5");
    }

    public LoginResult login() throws IOException, InterruptedException {
        String data = Requests.getGson().toJson(credentials);
        LoginResult result = Requests.post(header, Endpoints.LOGIN,data,LoginResult.class);
        header.put("Authorization","Bearer " + result.token);
        return result;
    }

    public boolean isLoggedIn() {
        return header.containsKey("Authorization") && header.get("Authorization").length() > 0;
    }

    public LogoutResult logout() throws IOException, InterruptedException {
        LogoutResult lr = Requests.delete(header, Endpoints.LOGOUT, Query.EMPTY_QUERY,LogoutResult.class);
        header.remove("Authorization");
        return lr;
    }


    public FormatsResult getFormats() throws IOException, InterruptedException {
        return Requests.get(header, Endpoints.FORMATS,Query.EMPTY_QUERY,FormatsResult.class);
    }

    public LanguagesResult getLanguages() throws IOException, InterruptedException {
        return Requests.get(header, Endpoints.LANGUAGES,Query.EMPTY_QUERY,LanguagesResult.class);
    }

    public UserResult getUserInfo() throws IOException, InterruptedException {
        return Requests.get(header, Endpoints.USER,Query.EMPTY_QUERY,UserResult.class);
    }

    public DiscoverResult getLatest(Query query) throws IOException, InterruptedException {
        return Requests.get(header, Endpoints.LATEST,query,DiscoverResult.class);
    }

    public DiscoverResult getPopular(Query query) throws IOException, InterruptedException {
        return Requests.get(header, Endpoints.POPULAR,query,DiscoverResult.class);
    }

    public DiscoverResult getMostDownloaded(Query query) throws IOException, InterruptedException {
        return Requests.get(header, Endpoints.MOST_DOWNLOADED,query,DiscoverResult.class);
    }

    public SubtitlesResult getSubtitles(Query query) throws IOException, InterruptedException {
        return Requests.get(header, Endpoints.SUBTITLES,query,SubtitlesResult.class);
    }

    public Feature[] getFeatures(Query query) throws IOException, InterruptedException {
        FeatureResult fr = Requests.get(header, Endpoints.FEATURES,query,FeatureResult.class);
        if ( fr == null ) {
            return new Feature[0];
        }
        return fr.data;
    }

    public GuessItResult guess(GuessItQuery query) throws IOException, InterruptedException {
        return Requests.get(header, Endpoints.GUESSIT,query,GuessItResult.class);
    }

    public DownloadLinkResult getDownloadLink(DownloadBody body) throws IOException, InterruptedException {
        //String data = gson.toJson(body);
        // The official body isn't working... Not sure why
        String data = "{\"file_id\": " + body.file_id + "}";
        return Requests.post(header, Endpoints.DOWNLOAD,data,DownloadLinkResult.class);
    }

    public DownloadLinkResult getDownloadLink(Subtitle.FileObject subFile) throws IOException, InterruptedException {
        DownloadBody body = new DownloadBody().setForceDownload(true).setFileId(subFile.file_id).setFileName(subFile.file_name).setSubFormat("srt").setTimeshift(0);
        return getDownloadLink(body);
    }

    public void download(DownloadLinkResult link, Path location) throws IOException {
        String save = location.toString();
        InputStream inputStream = new URL(link.link).openStream();
        Paths.get(save).getParent().toFile().mkdirs();
        Files.copy(inputStream, Paths.get(save), StandardCopyOption.REPLACE_EXISTING);
    }


}
