package com.soft.data.yandex;

import com.soft.data.*;
import com.soft.util.Browser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class YandexUser extends User {

    public YandexUser(String url) {
        super(url);
    }

    @Override
    public Storage storage() {
        return YandexStorage.instance();
    }

    @Override
    protected String parseName(Document document) {
        return Parser.parseFirst(document, "span[class^='user__name']", element -> element.text().strip());
    }

    @Override
    protected List<Track> parseTracks(Document document) {
        return Parser.parseFirst(Browser.connect(url + "/tracks").body(), "script[nonce]", element -> {
            String text = element.toString(), s = "\"trackIds\":[";
            int i = text.indexOf(s), j = text.indexOf(']', i);
            String[] split = text.substring(i + s.length(), j).split(",");
            List<Track> tracks = new ArrayList<>();
            for (String id : split) {
                String link = storage().makeURL("track/" + id.substring(1, id.length() - 1));
                tracks.add(storage().getTrack(link));
            }
            return tracks;
        });
    }

    @Override
    protected List<Album> parseAlbums(Document document) {
        List<Album> albums = new ArrayList<>();
        document = Browser.connect(url + "/albums");
        for (Element e : document.select("div[class^='album__title']")) {
            Album album = storage().getAlbum(e.child(0).attr("abs:href"));
            albums.add(album);
        }
        return albums;
    }

    @Override
    protected List<Artist> parseArtists(Document document) {
        List<Artist> artists = new ArrayList<>();
        document = Browser.connect(url + "/artists");
        for (Element e : document.select("span[class^='d-artists']")) {
            Artist artist = storage().getArtist(e.child(0).attr("abs:href"));
            artist.name().trySet(e.text().strip());
            artists.add(artist);
        }
        return artists;
    }

    @Override
    protected List<Playlist> parsePlaylists(Document document) {
        return Parser.parseFirst(document.body(), "script[nonce]", element -> {
            String text = element.toString(), s = "\"playlistIds\":[";
            int i = text.indexOf(s), j = text.indexOf(']', i += s.length());
            String[] split = text.substring(i, j).split(",");
            List<Playlist> playlists = new ArrayList<>();
            for (String id : split) playlists.add(storage().getPlaylist(url + "/playlists/" + id));
            return playlists;
        });
    }
}