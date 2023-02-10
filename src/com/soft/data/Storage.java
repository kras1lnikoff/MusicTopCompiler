package com.soft.data;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class Storage implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Map<String, Storage> INSTANCES = new HashMap<>();

    protected static Storage getInstance(String name, Supplier<Storage> newInstance) {
        return INSTANCES.computeIfAbsent(name, s -> newInstance.get());
    }

    private transient final File storedData;
    private transient boolean init;

    protected Map<String, Track> urlTracks;
    protected Map<String, Album> urlAlbums;
    protected Map<String, Artist> urlArtists;

    public Storage(File storedData) {
        this.storedData = storedData;
        this.urlTracks = new ConcurrentHashMap<>();
        this.urlAlbums = new ConcurrentHashMap<>();
        this.urlArtists = new ConcurrentHashMap<>();
    }

    public abstract String baseURL();

    public abstract boolean allowsUsers();

    public abstract User getUser(String url);

    public abstract Playlist getPlaylist(String url);

    public abstract String toUserURL(String name);

    public abstract String toPlaylistURL(String name);

    public abstract String toArtistURL(String name);

    public abstract String toAlbumURL(String name);

    public abstract String toTrackURL(String name);

    protected abstract Track newTrack(String url);

    protected abstract Album newAlbum(String url);

    protected abstract Artist newArtist(String url);

    @Override
    public String toString() {
        return baseURL();
    }

    public String makeURL(String url) {
        return this + "/" + url;
    }

    public File getStoredData() {
        return storedData;
    }

    public boolean containsTrack(String url) {
        return urlTracks.containsKey(url);
    }

    public boolean containsAlbum(String url) {
        return urlAlbums.containsKey(url);
    }

    public boolean containsArtist(String url) {
        return urlArtists.containsKey(url);
    }

    public void putTrack(Track track) {
        urlTracks.put(track.url(), track);
    }

    public void putAlbum(Album album) {
        urlAlbums.put(album.url(), album);
    }

    public void putArtist(Artist artist) {
        urlArtists.put(artist.url(), artist);
    }

    public Track getTrack(String url) {
        return urlTracks.computeIfAbsent(url, this::newTrack);
    }

    public Album getAlbum(String url) {
        return urlAlbums.computeIfAbsent(url, this::newAlbum);
    }

    public Artist getArtist(String url) {
        return urlArtists.computeIfAbsent(url, this::newArtist);
    }

    public User getUserByName(String name) {
        return getUser(toUserURL(name));
    }

    public Artist getArtistByName(String name) {
        return getArtist(toArtistURL(name));
    }

    public Set<Track> tracks() {
        return new HashSet<>(urlTracks.values());
    }

    public Set<Album> albums() {
        return new HashSet<>(urlAlbums.values());
    }

    public Set<Artist> artists() {
        return new HashSet<>(urlArtists.values());
    }

    public void init() {
        if (init) return;
        load();
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
        init = true;
    }

    public void load() {
        try {
            storedData.createNewFile();
            loadFrom(Files.newInputStream(storedData.toPath()));
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("An error occurred while loading data from \"" + storedData + "\"");
        }
    }

    public void save() {
        try {
            storedData.createNewFile();
            saveTo(Files.newOutputStream(storedData.toPath()));
        } catch (IOException e) {
            System.err.println("An error occurred while saving data to \"" + storedData + "\"");
        }
    }

    public void loadFrom(InputStream stream) throws IOException, ClassNotFoundException {
        if (stream.available() == 0) return;
        ObjectInputStream in = new ObjectInputStream(stream);
        Storage storage = (Storage) in.readObject();
        urlTracks = storage.urlTracks;
        urlAlbums = storage.urlAlbums;
        urlArtists = storage.urlArtists;
        in.close();
    }

    public void saveTo(OutputStream stream) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(this);
        out.close();
    }
}