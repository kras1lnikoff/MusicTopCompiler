package com.soft.data;

import org.jsoup.nodes.Document;

import java.util.List;

public abstract class Artist extends Entry {

    public Artist(String url) {
        super(url);
    }

    protected abstract String parseName(Document document);

    protected abstract String parseID(Document document);

    protected abstract List<Album> parseAlbums(Document document);

    protected abstract List<Track> parseTracks(Document document);

    @Override
    public String type() {
        return "artist";
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

    public String getID() {
        return id().get();
    }

    public List<Album> getAlbums() {
        return albums().get();
    }

    public List<Track> getTracks() {
        return tracks().get();
    }

    public Value<String> name() {
        return getValue("name", this::parseName);
    }

    public Value<String> id() {
        return getValue("id", this::parseID);
    }

    public Value<List<Album>> albums() {
        return getValue("albums", this::parseAlbums);
    }

    public Value<List<Track>> tracks() {
        return getValue("tracks", this::parseTracks);
    }
}