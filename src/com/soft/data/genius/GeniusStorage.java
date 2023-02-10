package com.soft.data.genius;

import com.soft.data.*;

import java.io.File;

public class GeniusStorage extends Storage {

    public static Storage instance() {
        return Storage.getInstance("genius", GeniusStorage::new);
    }

    public GeniusStorage() {
        super(new File("resources/genius.dat"));
    }

    @Override
    public String baseURL() {
        return "https://genius.com";
    }

    @Override
    public boolean allowsUsers() {
        return false;
    }

    @Override
    public User getUser(String url) {
        return null;
    }

    @Override
    public Playlist getPlaylist(String url) {
        return null;
    }

    @Override
    public String toUserURL(String name) {
        return null;
    }

    @Override
    public String toPlaylistURL(String name) {
        return null;
    }

    @Override
    public String toArtistURL(String name) {
        return makeURL("artists/" + name);
    }

    @Override
    public String toAlbumURL(String name) {
        return makeURL("albums/" + name);
    }

    @Override
    public String toTrackURL(String name) {
        return makeURL(name);
    }

    @Override
    protected Track newTrack(String url) {
        return new GeniusTrack(url);
    }

    @Override
    protected Album newAlbum(String url) {
        return new GeniusAlbum(url);
    }

    @Override
    protected Artist newArtist(String url) {
        return new GeniusArtist(url);
    }
}