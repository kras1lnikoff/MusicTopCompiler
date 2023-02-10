package com.soft;

import com.soft.data.*;
import com.soft.data.genius.GeniusStorage;
import com.soft.data.yandex.YandexStorage;
import com.soft.util.Browser;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.soft.Display.*;

public final class Selector {

    public static List<Track> selectTracks() {
        return new Selector().select();
    }

    private Storage storage;
    private Loader loader;
    private Set<Track> selectedTracks;

    private Selector() {
    }

    private List<Track> select() {
        selectedTracks = new HashSet<>();
        while ((storage = selectStorage()) != null) addStorage();
        List<Track> tracks = new ArrayList<>(selectedTracks);
        Collections.shuffle(tracks);
        return tracks;
    }

    private void userAction() {
        User user;
        while ((user = selectUser()) != null) addUser(user);
    }

    private void artistAction(List<Artist> allArtists) {
        Artist artist;
        while ((artist = selectArtist(allArtists)) != null) addArtist(artist);
    }

    private void albumAction(List<Album> allAlbums) {
        List<Album> albums;
        while (!(albums = selectAlbums(allAlbums)).isEmpty()) addAlbums(albums);
    }

    private void playlistAction(List<Playlist> allPlaylists) {
        List<Playlist> playlists;
        while (!(playlists = selectPlaylists(allPlaylists)).isEmpty()) addPlaylists(playlists);
    }

    private void trackAction(List<Track> allTracks) {
        List<Track> tracks;
        while (!(tracks = selectTracks(allTracks)).isEmpty()) addTracks(tracks);
    }

    private void addStorage() {
        System.out.printf("Storage selected: \"%s\"\n", storage);
        loader = new Loader(storage);
        loader.loadStorage();
        if (storage.allowsUsers()) userAction();
        artistAction(sorted(storage.artists()));
        albumAction(sorted(storage.albums()));
        trackAction(sorted(storage.tracks()));
    }

    private void addUser(User user) {
        String name = user.toString();
        System.out.printf("User selected: \"%s\"\n", name);
        loader.loadUser(user);
        playlistAction(user.getPlaylists());
        artistAction(user.getArtists());
        albumAction(user.getAlbums());
        trackAction(user.getTracks());
    }

    private void addArtist(Artist artist) {
        System.out.printf("Artist selected: \"%s\"\n", artist);
        loader.loadArtist(artist);
        albumAction(artist.getAlbums());
        trackAction(artist.getTracks());
    }

    private void addPlaylists(List<Playlist> playlists) {
        List<Track> all = new ArrayList<>();
        for (Playlist playlist : playlists) {
            System.out.printf("Playlist selected: \"%s\"\n", playlist);
            loader.loadAll(playlist.getTracks());
            all.addAll(playlist.getTracks());
        }
        if (!all.isEmpty()) addTracks(selectTracks(all));
    }

    private void addAlbums(List<Album> albums) {
        List<Track> all = new ArrayList<>();
        for (Album album : albums) {
            System.out.printf("Album selected: \"%s\"\n", album);
            loader.loadAll(album.getTracks());
            all.addAll(album.getTracks());
        }
        if (!all.isEmpty()) addTracks(selectTracks(all));
    }

    private void addTracks(List<Track> tracks) {
        for (Track track : tracks) if (selectedTracks.add(track)) System.out.printf("Track added: \"%s\"\n", track);
    }

    private Storage selectStorage() {
        List<Storage> storages = Arrays.asList(GeniusStorage.instance(), YandexStorage.instance());
        return single(Display.select(buildList(storages, false),
                "Select the storage:", "Storage selection", () -> null));
    }

    private User selectUser() {
        String result = ask("Type your username:", "User search");
        if (result == null || result.isEmpty()) return null;
        String url = storage.toUserURL(result);
        return Browser.exists(url) ? storage.getUser(url) : selectUser();
    }

    private Artist selectArtist(List<Artist> artists) {
        return single(select(artists, "artist", false, storage::toArtistURL, storage::getArtist));
    }

    private List<Playlist> selectPlaylists(List<Playlist> playlists) {
        return select(playlists, "playlist", true, storage::toPlaylistURL, storage::getPlaylist);
    }

    private List<Album> selectAlbums(List<Album> albums) {
        return select(albums, "album", true, storage::toAlbumURL, storage::getAlbum);
    }

    private List<Track> selectTracks(List<Track> tracks) {
        return select(tracks, "track", true, storage::toTrackURL, storage::getTrack);
    }

    private <T extends Entry> List<T> select(List<T> all, String type, boolean multiple,
                                             Function<String, String> parser, Function<String, T> loader) {
        List<T> values = distinct(all);
        String capital = Character.toUpperCase(type.charAt(0)) + type.substring(1);
        String search = "Select the " + (multiple ? type + "s" : type) + ":", title = capital + " selection";
        AtomicBoolean reselect = new AtomicBoolean();
        List<T> list = search(buildList(values, multiple), search, null, title, this::filter, input -> {
            if (input == null || input.isEmpty()) return null;
            String url = Browser.transform(parser.apply(input));
            if (url != null) {
                T loaded = loader.apply(url);
                String message = "%s \"%s\" was found. Load it?".formatted(capital, loaded);
                if (confirm(message, "Search result")) return loaded;
            }
            reselect.set(true);
            return null;
        });
        if (reselect.get()) list.addAll(select(all, type, multiple, parser, loader));
        return list;
    }

    private <T extends Entry> List<T> distinct(List<T> values) {
        Set<String> met = new HashSet<>();
        List<T> list = new ArrayList<>();
        for (T value : values) {
            if (!value.isLoaded() || met.contains(value.toString())) continue;
            list.add(value);
            met.add(value.toString());
        }
        return list;
    }

    private <T extends Entry> List<T> filter(List<T> values, String filter) {
        if (filter.isEmpty()) return values;
        String substring = filter.toLowerCase(Locale.ROOT);
        Comparator<T> comparator = Comparator.<T>comparingInt(t -> lower(t).indexOf(substring))
                .thenComparing(t -> -lower(t).split(substring, -1).length).thenComparing(Object::toString);
        return values.stream().filter(t -> lower(t).contains(substring)).sorted(comparator).toList();
    }

    private <T extends Entry> List<T> sorted(Set<T> set) {
        List<T> list = distinct(new ArrayList<>(set));
        list.sort(Comparator.comparing(Object::toString));
        return list;
    }

    private String lower(Object t) {
        return t.toString().toLowerCase(Locale.ROOT);
    }

    static <T> JList<T> buildList(List<T> values, boolean multiple) {
        JList<T> list = new JList<>(new Vector<>(values));
        list.setVisibleRowCount(Math.min(values.size(), 10));
        if (!multiple) list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_F1) return;
                int index = list.getLeadSelectionIndex();
                if (index < 0 || index >= list.getModel().getSize()) return;
                String url = getURL(list.getModel().getElementAt(index));
                if (url != null) Browser.browse(url);
            }
        });
        return list;
    }

    private static String getURL(Object item) {
        if (item instanceof Track) {
            Track track = (Track) item;
            return track.video().isEmpty() ? track.url() : track.getVideo();
        }
        if (item instanceof Entry) return ((Entry) item).url();
        if (item instanceof Storage) return ((Storage) item).baseURL();
        return null;
    }
}