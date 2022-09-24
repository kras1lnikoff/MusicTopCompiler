package com.soft.data;

import com.soft.util.Browser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Album extends Entry {

    private String title;
    private List<Song> songs;

    private Artist artist;

    private LocalDate releaseDate;

    public Album(String url) {
        super(url);
    }

    @Override
    public String toString() {
        return title();
    }

    public String title() {
        return get(title, this::setTitle, this::parseTitle);
    }

    public List<Song> songs() {
        return get(songs, this::setSongs, this::parseSongs);
    }

    public Artist artist() {
        return get(artist, this::setArtist, this::parseArtist);
    }

    public LocalDate releaseDate() {
        return get(releaseDate, this::setReleaseDate, this::parseReleaseDate);
    }

    public void trySetTitle(Supplier<String> supplier) {
        trySet(title, supplier, this::setTitle);
    }

    public void trySetSongs(Supplier<List<Song>> supplier) {
        trySet(songs, supplier, this::setSongs);
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

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    private String parseTitle(Document document) {
        return Browser.cutTitle(document, "Lyrics and Tracklist | Genius");
    }

    private List<Song> parseSongs(Document document) {
        List<Song> songs = new ArrayList<>();
        for (Element e : document.body().select("h3[class='chart_row-content-title']")) {
            Song song = Storage.getSong(e.parent().attr("href"));
            song.trySetTitle(() -> title() + " - " + e.ownText().strip());
            song.trySetAlbum(() -> this);
            songs.add(song);
        }
        return songs;
    }

    private Artist parseArtist(Document document) {
        return parseData(document, "a[href^='https://genius.com/artists/']",
                e -> Storage.getArtist(e.attr("abs:href")));
    }

    private LocalDate parseReleaseDate(Document document) {
        return parseData(document, "div[class^='metadata_unit']",
                e -> Browser.parseDate(e.text().substring("Released ".length())));
    }

    private <T> T parseData(Document document, String query, Function<Element, T> parser) {
        Element element = document.body().selectFirst(query);
        return element == null ? null : parser.apply(element);
    }
}