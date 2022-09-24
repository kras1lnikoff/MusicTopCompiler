package com.soft.data;

import com.soft.util.Browser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class Artist extends Entry {

    private String name;
    private String id;

    private List<Album> albums;
    private List<Song> songs;

    public Artist(String url) {
        super(url);
    }

    @Override
    public String toString() {
        return name();
    }

    public String name() {
        return get(name, this::setName, this::parseName);
    }

    public String id() {
        return get(id, this::setID, this::parseID);
    }

    public List<Album> albums() {
        return get(albums, this::setAlbums, this::parseAlbums);
    }

    public List<Song> songs() {
        return get(songs, this::setSongs, this::parseSongs);
    }

    public void trySetName(Supplier<String> supplier) {
        trySet(name, supplier, this::setName);
    }

    public void trySetID(Supplier<String> supplier) {
        trySet(id, supplier, this::setID);
    }

    public void trySetAlbums(Supplier<List<Album>> supplier) {
        trySet(albums, supplier, this::setAlbums);
    }

    public void trySetSongs(Supplier<List<Song>> supplier) {
        trySet(songs, supplier, this::setSongs);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    private String parseName(Document document) {
        String name = Browser.cutTitle(document, "Lyrics, Songs, and Albums | Genius");
        return name != null ? name : Browser.cutTitle(document, "| Genius");
    }

    private String parseID(Document document) {
        Element e = document.head().selectFirst("meta[content^='/artists/']");
        return e == null ? "" : e.attr("content").substring("/artists/".length());
    }

    private List<Album> parseAlbums(Document document) {
        List<Album> albums = new ArrayList<>();
        document = Browser.connect("https://genius.com/artists/albums?for_artist_page=" + id());
        for (Element e : document.body().select("a[href^='/albums']")) {
            Album album = Storage.getAlbum(e.attr("abs:href"));
            album.trySetTitle(() -> name() + " - " + e.text().strip());
            album.trySetArtist(() -> this);
            albums.add(album);
        }
        albums.sort(Comparator.comparing(Album::title));
        return albums;
    }

    private List<Song> parseSongs(Document document) {
        List<Song> songs = new ArrayList<>();
        String url = "https://genius.com/artists/songs?for_artist_page=" + id();
        for (int page = 1; ; page++) {
            document = Browser.connect(url + "&page=" + page);
            Elements select = document.body().select("span[class='song_title']");
            if (select.isEmpty()) break;
            for (Element e : select) {
                Song song = Storage.getSong(e.parent().parent().attr("abs:href"));
                song.trySetTitle(() -> (song.isSingle() ? song.artist() : song.album()) + " - " + e.text().strip());
                songs.add(song);
            }
        }
        songs.sort(Comparator.comparing(Song::title));
        return songs;
    }
}