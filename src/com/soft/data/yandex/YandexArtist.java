package com.soft.data.yandex;

import com.soft.data.*;
import com.soft.util.Browser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class YandexArtist extends Artist {

    public YandexArtist(String url) {
        super(url);
    }

    @Override
    public Storage storage() {
        return YandexStorage.instance();
    }

    @Override
    protected String parseName(Document document) {
        return Parser.parseFirst(document, "h1[class^='page-artist__title']", element -> element.text().strip());
    }

    @Override
    protected String parseID(Document document) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    @Override
    protected List<Album> parseAlbums(Document document) {
        document = Browser.connect(url + "/albums");
        Element el = document.selectFirst("div[class='page-artist__albums']");
        Elements select = el.select("div[class^='album__title']");
        if (select.size() < 100) {
            List<Album> albums = new ArrayList<>();
            for (Element e : select) {
                Album album = storage().getAlbum(e.child(0).attr("abs:href"));
                album.title().trySet(e.text().strip());
                album.artist().trySet(this);
                albums.add(album);
            }
            return albums;
        } else return Parser.parseFirst(document.body(), "script[nonce]", element -> {
            String text = element.toString(), s = "\"albumIds\":[";
            int i = text.indexOf(s), j = text.indexOf(']', i += s.length());
            String[] split = text.substring(i, j).split(",");
            List<Album> albums = new ArrayList<>();
            for (String id : split) {
                String link = storage().makeURL("album/" + id);
                albums.add(storage().getAlbum(link));
            }
            return albums;
        });
    }

    @Override
    protected List<Track> parseTracks(Document document) {
        document = Browser.connect(url + "/tracks");
        Elements select = document.body().select("div[class='d-track__name']");
        if (select.size() < 100) {
            List<Track> tracks = new ArrayList<>();
            for (Element e : select) {
                Track track = storage().getTrack(e.child(0).attr("abs:href"));
                track.title().trySet(e.text().strip());
                track.artist().trySet(this);
                tracks.add(track);
            }
            return tracks;
        } else return Parser.parseFirst(document.body(), "script[nonce]", element -> {
            String text = element.toString(), s = "\"trackIds\":[";
            int i = text.indexOf(s), j = text.indexOf(']', i += s.length());
            String[] split = text.substring(i, j).split(",");
            List<Track> tracks = new ArrayList<>();
            for (String id : split) {
                String link = storage().makeURL("track/" + id.substring(1, id.length() - 1));
                tracks.add(storage().getTrack(link));
            }
            return tracks;
        });
    }
}