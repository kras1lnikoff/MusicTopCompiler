package com.soft.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Storage implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Storage INSTANCE;

    private static Storage instance() {
        return INSTANCE == null ? INSTANCE = new Storage() : INSTANCE;
    }

    private final Map<String, Song> allSongs = new HashMap<>();
    private final Map<String, Album> allAlbums = new HashMap<>();
    private final Map<String, Artist> allArtists = new HashMap<>();

    private Storage() {
    }

    public static void putSong(Song song) {
        instance().allSongs.put(song.url(), song);
    }

    public static void putAlbum(Album album) {
        instance().allAlbums.put(album.url(), album);
    }

    public static void putArtist(Artist artist) {
        instance().allArtists.put(artist.url(), artist);
    }

    public static Song getSong(String url) {
        return instance().allSongs.computeIfAbsent(url, Song::new);
    }

    public static Album getAlbum(String url) {
        return instance().allAlbums.computeIfAbsent(url, Album::new);
    }

    public static Artist getArtist(String url) {
        return instance().allArtists.computeIfAbsent(url, Artist::new);
    }

    public static int songsTotal() {
        return instance().allSongs.size();
    }

    public static int albumsTotal() {
        return instance().allAlbums.size();
    }

    public static int artistsTotal() {
        return instance().allArtists.size();
    }

    public static Collection<Song> songs() {
        return instance().allSongs.values();
    }

    public static Collection<Album> albums() {
        return instance().allAlbums.values();
    }

    public static Collection<Artist> artists() {
        return instance().allArtists.values();
    }

    private static final File STORAGE_DATA = new File("resources/storage.dat");

    private static boolean init = false;

    public static void init() {
        if (init) return;
        Storage.load();
        Runtime.getRuntime().addShutdownHook(new Thread(Storage::save, "Storage save"));
        init = true;
    }

    public static void load() {
        try {
            loadFrom(Files.newInputStream(STORAGE_DATA.toPath(), StandardOpenOption.CREATE));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save() {
        try {
            saveTo(Files.newOutputStream(STORAGE_DATA.toPath(), StandardOpenOption.CREATE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadFrom(InputStream stream) throws IOException, ClassNotFoundException {
        if (stream.available() == 0) return;
        ObjectInputStream in = new ObjectInputStream(stream);
        INSTANCE = (Storage) in.readObject();
        in.close();
    }

    public static void saveTo(OutputStream stream) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(instance());
        out.close();
    }
}