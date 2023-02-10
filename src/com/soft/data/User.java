package com.soft.data;

import org.jsoup.nodes.Document;

import java.util.List;

public abstract class User extends Entry {

    public User(String url) {
        super(url);
    }

    protected abstract String parseName(Document document);

    protected abstract List<Track> parseTracks(Document document);

    protected abstract List<Album> parseAlbums(Document document);

    protected abstract List<Artist> parseArtists(Document document);

    protected abstract List<Playlist> parsePlaylists(Document document);

    @Override
    public String type() {
        return "user";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isLoaded() {
        return name().isLoaded();
    }

    public String getName() {
        return name().get();
    }

    public List<Track> getTracks() {
        return tracks().get();
    }

    public List<Album> getAlbums() {
        return albums().get();
    }

    public List<Artist> getArtists() {
        return artists().get();
    }

    public List<Playlist> getPlaylists() {
        return playlists().get();
    }

    public Value<String> name() {
        return getValue("name", this::parseName);
    }

    public Value<List<Track>> tracks() {
        return getValue("tracks", this::parseTracks);
    }

    public Value<List<Album>> albums() {
        return getValue("albums", this::parseAlbums);
    }

    public Value<List<Artist>> artists() {
        return getValue("artists", this::parseArtists);
    }

    public Value<List<Playlist>> playlists() {
        return getValue("playlists", this::parsePlaylists);
    }
}