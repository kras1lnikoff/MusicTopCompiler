package com.soft.data;

import org.jsoup.nodes.Document;

import java.util.List;

public abstract class Playlist extends Entry {

    public Playlist(String url) {
        super(url);
    }

    protected abstract String parseTitle(Document document);

    protected abstract List<Track> parseTracks(Document document);

    protected abstract User parseUser(Document document);

    protected abstract String parseUpdateDate(Document document);

    @Override
    public String type() {
        return "playlist";
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean isLoaded() {
        return title().isLoaded();
    }

    public String getTitle() {
        return title().get();
    }

    public List<Track> getTracks() {
        return tracks().get();
    }

    public User getUser() {
        return user().get();
    }

    public String getUpdateDate() {
        return updateDate().get();
    }

    public Value<String> title() {
        return getValue("title", this::parseTitle);
    }

    public Value<List<Track>> tracks() {
        return getValue("tracks", this::parseTracks);
    }

    public Value<User> user() {
        return getValue("user", this::parseUser);
    }

    public Value<String> updateDate() {
        return getValue("updateDate", this::parseUpdateDate);
    }
}