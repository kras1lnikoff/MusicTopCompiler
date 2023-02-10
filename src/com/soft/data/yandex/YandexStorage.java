package com.soft.data.yandex;

import com.soft.data.*;

import java.io.File;

public class YandexStorage extends Storage {

    public static Storage instance() {
        return Storage.getInstance("yandex", YandexStorage::new);
    }

    public YandexStorage() {
        super(new File("resources/yandex.dat"));
    }

    @Override
    public String baseURL() {
        return "https://music.yandex.com";
    }

    @Override
    public boolean allowsUsers() {
        return true;
    }

    @Override
    public User getUser(String url) {
        return new YandexUser(url);
    }

    @Override
    public Playlist getPlaylist(String url) {
        return new YandexPlaylist(url);
    }

    @Override
    public String toUserURL(String name) {
        return makeURL("users/" + name);
    }

    @Override
    public String toPlaylistURL(String name) {
        int index = name.lastIndexOf('/');
        String user = name.substring(0, index), id = name.substring(index + 1);
        return makeURL("users/" + user + "/playlists/" + id);
    }

    @Override
    public String toArtistURL(String name) {
        return makeURL("artist/" + name);
    }

    @Override
    public String toAlbumURL(String name) {
        return makeURL("album/" + name);
    }

    @Override
    public String toTrackURL(String name) {
        return makeURL("track/" + name);
    }

    @Override
    protected Track newTrack(String url) {
        return new YandexTrack(url);
    }

    @Override
    protected Album newAlbum(String url) {
        return new YandexAlbum(url);
    }

    @Override
    protected Artist newArtist(String url) {
        return new YandexArtist(url);
    }

    @Override
    public Track getTrack(String url) {
        return super.getTrack(YandexTrack.formatURL(url));
    }
}