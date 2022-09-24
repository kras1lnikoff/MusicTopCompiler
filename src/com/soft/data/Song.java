package com.soft.data;

import com.soft.util.Browser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Supplier;

public class Song extends Entry {

    private String title;
    private String video;
    private String text;

    private Album album;
    private Artist artist;

    private LocalDate releaseDate;

    public Song(String url) {
        super(url);
    }

    @Override
    public String toString() {
        return title();
    }

    public String title() {
        return get(title, this::setTitle, this::parseTitle);
    }

    public String video() {
        return get(video, this::setVideo, this::parseVideo);
    }

    public String text() {
        return get(text, this::setText, this::parseText);
    }

    public Album album() {
        return get(album, this::setAlbum, this::parseAlbum);
    }

    public Artist artist() {
        return get(artist, this::setArtist, this::parseArtist);
    }

    public LocalDate releaseDate() {
        return get(releaseDate, this::setReleaseDate, this::parseReleaseDate);
    }

    public boolean isSingle() {
        return album() == null;
    }

    public void trySetTitle(Supplier<String> supplier) {
        trySet(title, supplier, this::setTitle);
    }

    public void trySetVideo(Supplier<String> supplier) {
        trySet(video, supplier, this::setVideo);
    }

    public void trySetText(Supplier<String> supplier) {
        trySet(text, supplier, this::setText);
    }

    public void trySetAlbum(Supplier<Album> supplier) {
        trySet(album, supplier, this::setAlbum);
    }

    public void trySetArtist(Supplier<Artist> supplier) {
        trySet(artist, supplier, this::setArtist);
    }

    public void trySetReleaseDate(Supplier<LocalDate> supplier) {
        trySet(releaseDate, supplier, this::setReleaseDate);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    private String parseTitle(Document document) {
        return (isSingle() ? artist() : album()) + " - " + Browser.cutTitle(document, "Lyrics | Genius Lyrics");
    }

    private String parseVideo(Document document) {
        for (Element script : document.body().select("script")) {
            String text = script.toString(), s = "http://www.youtube.com/watch?v=";
            int index = text.indexOf(s), from = index + s.length();
            if (index >= 0) return text.substring(index, text.indexOf('\\', from));
        }
        return "";
    }

    private String parseText(Document document) {
        StringBuilder builder = new StringBuilder();
        for (Element e : document.body().select("div[^data-lyrics]")) builder.append(e.wholeText());
        return builder.toString();
    }

    private Album parseAlbum(Document document) {
        return parseData(document, "div[class^='PrimaryAlbum__AlbumDetails']",
                e -> Storage.getAlbum(e.child(0).attr("abs:href")));
    }

    private Artist parseArtist(Document document) {
        return parseData(document, "a[href^='https://genius.com/artists/']",
                e -> Storage.getArtist(e.attr("abs:href")));
    }

    private LocalDate parseReleaseDate(Document document) {
        return parseData(document, "div[class^='HeaderMetadata__ReleaseDate']",
                e -> Browser.parseDate(e.text()));
    }

    private <T> T parseData(Document document, String query, Function<Element, T> parser) {
        Element element = document.body().selectFirst(query);
        return element == null ? null : parser.apply(element);
    }
}