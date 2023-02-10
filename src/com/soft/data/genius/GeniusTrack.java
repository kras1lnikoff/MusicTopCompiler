package com.soft.data.genius;

import com.soft.data.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class GeniusTrack extends Track {

    public GeniusTrack(String url) {
        super(url);
    }

    @Override
    public Storage storage() {
        return GeniusStorage.instance();
    }

    @Override
    protected String parseTitle(Document document) {
        return Parser.parseFirst(document.body(), "span[class^='SongHeader']", Element::text);
    }

    @Override
    protected String parseVideo(Document document) {
        for (Element script : document.body().select("script")) {
            String text = script.toString(), s = "http://www.youtube.com/watch?v=";
            int index = text.indexOf(s), from = index + s.length();
            if (index >= 0) return text.substring(index, text.indexOf('\\', from));
        }
        return null;
    }

    @Override
    protected String parseText(Document document) {
        StringBuilder builder = new StringBuilder();
        for (Element e : document.body().select("div[^data-lyrics]")) builder.append(e.wholeText());
        return builder.toString();
    }

    @Override
    protected Album parseAlbum(Document document) {
        return Parser.parseFirst(document, "div[class^='PrimaryAlbum__AlbumDetails']",
                element -> storage().getAlbum(element.child(0).attr("abs:href")));
    }

    @Override
    protected Artist parseArtist(Document document) {
        return Parser.parseFirst(document, "a[href^='" + storage().makeURL("artists") + "']",
                element -> storage().getArtist(element.attr("abs:href")));
    }

    @Override
    protected String parseReleaseDate(Document document) {
        return Parser.parseFirst(document, "div[class^='HeaderMetadata__ReleaseDate']", Element::text);
    }
}