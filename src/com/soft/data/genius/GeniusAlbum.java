package com.soft.data.genius;

import com.soft.data.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class GeniusAlbum extends Album {

    public GeniusAlbum(String url) {
        super(url);
    }

    @Override
    public Storage storage() {
        return GeniusStorage.instance();
    }

    @Override
    protected String parseTitle(Document document) {
        return Parser.parseFirst(document.body(), "h2[class^='text_label']",
                element -> element.text().substring(0, element.text().lastIndexOf(" Tracklist")));
    }

    @Override
    protected List<Track> parseTracks(Document document) {
        List<Track> tracks = new ArrayList<>();
        for (Element e : document.body().select("h3[class='chart_row-content-title']")) {
            Track track = storage().getTrack(e.parent().attr("href"));
            track.title().set(e.ownText());
            track.album().set(this);
            tracks.add(track);
        }
        return tracks;
    }

    @Override
    protected Artist parseArtist(Document document) {
        return Parser.parseFirst(document, "a[href^='" + storage().makeURL("artists") + "']",
                element -> storage().getArtist(element.attr("abs:href")));
    }

    @Override
    protected String parseReleaseDate(Document document) {
        return Parser.parseFirst(document, "div[class^='metadata_unit']",
                element -> element.text().substring("Released ".length()));
    }
}