package com.soft.data.genius;

import com.soft.data.Album;
import com.soft.data.Artist;
import com.soft.data.Track;
import com.soft.data.Storage;
import com.soft.util.Browser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class GeniusArtist extends Artist {

    public GeniusArtist(String url) {
        super(url);
    }

    @Override
    public Storage storage() {
        return GeniusStorage.instance();
    }

    @Override
    protected String parseName(Document document) {
        String name = Browser.cutTitle(document, "Lyrics, Tracks, and Albums | Genius");
        return name != null ? name : Browser.cutTitle(document, "| Genius");
    }

    @Override
    protected String parseID(Document document) {
        Element e = document.head().selectFirst("meta[content^='/artists/']");
        return e == null ? null : e.attr("content").substring("/artists/".length());
    }

    @Override
    protected List<Album> parseAlbums(Document document) {
        List<Album> albums = new ArrayList<>();
        document = Browser.connect(storage().makeURL("artists/albums?for_artist_page=" + id()));
        for (Element e : document.body().select("a[href^='/albums']")) {
            Album album = storage().getAlbum(e.attr("abs:href"));
            album.title().set(e.text().strip());
            album.artist().set(this);
            albums.add(album);
        }
        return albums;
    }

    @Override
    protected List<Track> parseTracks(Document document) {
        List<Track> tracks = new ArrayList<>();
        String url = storage().makeURL("artists/songs?for_artist_page=" + id());
        for (int page = 1; ; page++) {
            document = Browser.connect(url + "&page=" + page);
            Elements select = document.body().select("span[class='song_title']");
            if (select.isEmpty()) break;
            for (Element e : select) {
                Track track = storage().getTrack(e.parent().parent().attr("abs:href"));
                track.title().set(e.text().strip());
                tracks.add(track);
            }
        }
        return tracks;
    }
}