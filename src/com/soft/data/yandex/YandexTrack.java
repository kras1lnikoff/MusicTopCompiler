package com.soft.data.yandex;

import com.soft.data.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class YandexTrack extends Track {

    public YandexTrack(String url) {
        super(formatURL(url));
    }

    protected static String formatURL(String url) {
        return YandexStorage.instance().makeURL(url.substring(url.lastIndexOf("track/")));
    }

    @Override
    public Storage storage() {
        return YandexStorage.instance();
    }

    @Override
    protected String parseTitle(Document document) {
        return Parser.parseFirst(document, "a[class='d-link deco-link'][href*='/track/']", element -> element.text().strip());
    }

    @Override
    protected String parseVideo(Document document) {
        return null;
    }

    @Override
    protected String parseText(Document document) {
        return Parser.parseFirst(document.body(), "script[nonce]", element -> {
            String text = element.toString(), s = "\"fullLyrics\":\"";
            int i = text.indexOf(s), j = text.indexOf("\",\"hasRights\"", i += s.length());
            return text.substring(i, j).replace("\\n", "\n").replace("\\", "");
        });
    }

    @Override
    protected Album parseAlbum(Document document) {
        return Parser.parseFirst(document, "meta[property='music:album']",
                element -> storage().getAlbum(getContent(element)));
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
        return Parser.parseFirst(document, "span[class='typo deco-typo-secondary']", element -> element.text().strip());
    }
}