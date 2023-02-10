package com.soft.data.yandex;

import com.soft.data.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class YandexPlaylist extends Playlist {

    public YandexPlaylist(String url) {
        super(url);
    }

    @Override
    public Storage storage() {
        return YandexStorage.instance();
    }

    @Override
    protected String parseTitle(Document document) {
        return Parser.parseFirst(document, "h1[class^='page-playlist__title']", element -> element.text().strip());
    }

    @Override
    protected List<Track> parseTracks(Document document) {
        Elements select = document.body().select("div[class='d-track__name']");
        if (select.size() < 100) {
            List<Track> tracks = new ArrayList<>();
            for (Element e : select) {
                Track track = storage().getTrack(e.child(0).attr("abs:href"));
                track.title().trySet(e.text().strip());
                tracks.add(track);
            }
            return tracks;
        } else return Parser.parseFirst(document.body(), "script[nonce]", element -> {
            String text = element.toString(), s = "\"trackIds\":[";
            int i = text.indexOf(s), j = text.indexOf(']', i += s.length());
            String[] split = text.substring(i, j).split(",");
            List<Track> tracks = new ArrayList<>();
            for (String id : split) {
                String[] ids = id.substring(1, id.length() - 1).split(":");
                String link = storage().makeURL("track/" + ids[0]);
                tracks.add(storage().getTrack(link));
            }
            return tracks;
        });
    }

    @Override
    protected User parseUser(Document document) {
        int start = url.indexOf("/users/") + 7, end = url.lastIndexOf("/playlists/");
        return storage().getUserByName(url.substring(start, end));
    }

    @Override
    protected String parseUpdateDate(Document document) {
        return Parser.parseFirst(document.body(), "div[class='page-playlist__info-wrapper']",
                element -> element.ownText().substring(17));
    }
}