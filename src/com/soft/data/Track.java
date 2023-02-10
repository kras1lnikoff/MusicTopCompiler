package com.soft.data;

import org.jsoup.nodes.Document;

public abstract class Track extends Entry {

    public Track(String url) {
        super(url);
    }

    protected abstract String parseTitle(Document document);

    protected abstract String parseVideo(Document document);

    protected abstract String parseText(Document document);

    protected abstract Album parseAlbum(Document document);

    protected abstract Artist parseArtist(Document document);

    protected abstract String parseReleaseDate(Document document);

    @Override
    public String type() {
        return "track";
    }

    @Override
    public String toString() {
        return (album().isEmpty() ? artist() : album()) + " - " + title();
    }

    @Override
    public boolean isLoaded() {
        return album().isLoaded() && (!album().isEmpty() || artist().isLoaded()) && title().isLoaded();
    }

    public String getTitle() {
        return title().get();
    }

    public String getVideo() {
        return video().get();
    }

    public String getText() {
        return text().get();
    }

    public Album getAlbum() {
        return album().get();
    }

    public Artist getArtist() {
        return artist().get();
    }

    public String getReleaseDate() {
        return releaseDate().get();
    }

    public Value<String> title() {
        return getValue("title", this::parseTitle);
    }

    public Value<String> video() {
        return getValue("video", this::parseVideo);
    }

    public Value<String> text() {
        return getValue("text", this::parseText);
    }

    public Value<Album> album() {
        return getValue("album", this::parseAlbum);
    }

    public Value<Artist> artist() {
        return getValue("artist", this::parseArtist);
    }

    public Value<String> releaseDate() {
        return getValue("releaseDate", this::parseReleaseDate);
    }
}