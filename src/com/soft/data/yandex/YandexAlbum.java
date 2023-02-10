package com.soft.data.yandex;

import com.soft.data.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class YandexAlbum extends Album {

    public YandexAlbum(String url) {
        super(url);
    }

    @Override
    public Storage storage() {
        return YandexStorage.instance();
    }

    @Override
    public String toString() {
        return (artist().isEmpty() ? "Various artists" : artist()) + " - " + title();
    }

    @Override
    protected String parseTitle(Document document) {
        return Parser.parseFirst(document, "span[class='deco-typo']", element -> element.text().strip());
    }

    @Override
    protected List<Track> parseTracks(Document document) {
        Elements select = document.body().select("div[class='d-track__name']");
        if (select.size() < 100) {
            List<Track> tracks = new ArrayList<>();
            for (Element e : select) {
                Track track = storage().getTrack(e.child(0).attr("abs:href"));
                track.title().trySet(e.text().strip());
                track.album().trySet(this);
                tracks.add(track);
            }
            return tracks;
        } else return Parser.parseFirst(document, "script[nonce]", element -> {
            String text = element.toString(), s = "\"realId\":\"";
            List<Track> tracks = new ArrayList<>();
            int index = 0, i;
            while ((i = text.indexOf(s, index)) != -1) {
                index = text.indexOf('"', i += s.length());
                int id = Integer.parseInt(text.substring(i, index));
                tracks.add(storage().getTrack(url + "/track/" + id));
            }
            return tracks;
        });
    }

    @Override
    protected Artist parseArtist(Document document) {
        return Parser.parseFirst(document, "meta[property='music:musician']",
                element -> storage().getArtist(getContent(element)));
    }

    private String getContent(Element element) {
        String link = element.attr("content"), end = "?lang=en";
        return link.replace(end, "");
    }

    @Override
    protected String parseReleaseDate(Document document) {
        return Parser.parseFirst(document, "span[class='typo deco-typo-secondary']",
                element -> element.ownText().strip());
    }
}