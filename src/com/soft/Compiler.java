package com.soft;

import com.soft.data.Album;
import com.soft.data.Artist;
import com.soft.data.Song;
import com.soft.data.Storage;
import com.soft.util.Browser;
import com.soft.util.TopPriorityQueue;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public final class Compiler {

    public static void main(String[] args) {
        loadStorage();
        while (true) compile();
    }

    public static void loadStorage() {
        load(Storage::init);
    }

    public static void loadArtist(String name) {
        String url = Browser.transform("https://genius.com/artists/" + name);
        if (url == null) System.out.println("No artist found with url: " + url);
        else loadArtist(Storage.getArtist(url));
    }

    private static void load(Runnable action) {
        long start = System.currentTimeMillis();
        Thread thread = new Thread(action);
        thread.start();
        int songs = Storage.songsTotal(), albums = Storage.albumsTotal(), artists = Storage.artistsTotal();
        while (thread.isAlive()) {
            String info = buildStorageInfo(start, songs, albums, artists);
            System.out.print(info);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print("\b".repeat(info.length()));
        }
        System.out.println(buildStorageInfo(start, songs, albums, artists));
    }

    private static String buildStorageInfo(long start, int songs, int albums, int artists) {
        return "Loaded %d songs, %d albums and %d artists in %.2f seconds."
                .formatted(Storage.songsTotal() - songs, Storage.albumsTotal() - albums,
                        Storage.artistsTotal() - artists, (System.currentTimeMillis() - start) * 1e-3);
    }

    public static void compile() {
        System.out.println("Preparing songs...");
        List<Song> songs = selectSongs();
        System.out.printf("Preparing finished. %d songs were selected.\n", songs.size());
        int maxSize = selectMaxSize();
        System.out.printf("Maximum top size is %d.\n", maxSize);
        System.out.println("Loading...");
        Map<Map.Entry<Song, Song>, Integer> cache = new HashMap<>();
        Comparator<Song> comparator = (x, y) -> {
            if (x.equals(y)) return 0;
            Integer result = cache.get(Map.entry(x, y));
            if (result != null) return result;
            result = compareSongs(x, y);
            cache.put(Map.entry(x, y), result);
            cache.put(Map.entry(y, x), -result);
            return result;
        };
        List<Song> top = TopPriorityQueue.from(songs, comparator, maxSize).sorted();
        System.out.printf("The top compilation is complete. %d comparisons were made.\n", cache.size() / 2);
        Collections.reverse(top);
        displayTop(top);
        System.out.printf("The result top %d:\n", maxSize);
        for (int i = 0; i < top.size(); i++) System.out.printf("%d.  %s\n", i + 1, top.get(i));
    }

    private static List<Song> selectSongs() {
        Set<Song> selectedSongs = new HashSet<>();
        Artist artist;
        while ((artist = selectArtist()) != null) {
            System.out.printf("Artist selected: \"%s\".\n", artist);
            loadArtist(artist);
            List<Album> albums;
            while ((albums = selectAlbums(artist)) != null) {
                for (Album album : albums) {
                    selectedSongs.addAll(album.songs());
                    System.out.printf("Album added: \"%s\" (%d songs).\n", album, album.songs().size());
                }
            }
            List<Song> songs;
            while ((songs = selectSongs(artist)) != null) {
                for (Song song : songs) {
                    selectedSongs.add(song);
                    System.out.printf("Song added: \"%s\".\n", song);
                }
            }
        }
        List<Song> songs = new ArrayList<>(selectedSongs);
        Collections.shuffle(songs);
        return songs;
    }

    private static Artist selectArtist() {
        List<Artist> artists = new ArrayList<>(Storage.artists());
        artists.sort(Comparator.comparing(Artist::name));
        String result = askInput("Type the artist's name:", "Artist search");
        if (result == null) return null;
        JList<Artist> list = filteredList(artists, result);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showDialog(list, "Select the artist:", "Artist selection", JOptionPane.QUESTION_MESSAGE);
        if (!list.isSelectionEmpty()) return list.getSelectedValue();
        if (!result.isEmpty()) {
            String url = Browser.transform("https://genius.com/artists/" + result);
            if (url != null) {
                Artist artist = Storage.getArtist(url);
                int option = JOptionPane.showConfirmDialog(null,
                        "Artist \"%s\" was found on the web. Load him?".formatted(artist),
                        "Search result", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) return artist;
            }
        }
        return selectArtist();
    }

    public static void loadArtist(Artist artist) {
        load(() -> {
            artist.name();
            for (Album album : artist.albums()) {
                album.title();
                for (Song song : album.songs()) {
                    song.title();
                    song.dispose();
                }
                album.dispose();
            }
            for (Song song : artist.songs()) {
                song.title();
                song.dispose();
            }
            artist.dispose();
        });
        System.out.printf("%d songs and %d albums by \"%s\" were found.\n", artist.songs().size(), artist.albums().size(), artist);
    }

    private static List<Album> selectAlbums(Artist artist) {
        return search(artist.albums(), "Type the album title:",
                "Album search", "Select the albums:", "Album selection");
    }

    private static List<Song> selectSongs(Artist artist) {
        return search(artist.songs(), "Type the song title:",
                "Song search", "Select the songs:", "Song selection");
    }

    private static int selectMaxSize() {
        try {
            return Integer.parseInt(askInput("Type the maximum top size:", "Final configuration"));
        } catch (Exception e) {
            return selectMaxSize();
        }
    }

    private static String askInput(String message, String title) {
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    private static int compareSongs(Song x, Song y) {
        JList<Song> list = new JList<>(new Song[]{x, y});
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showDialog(list, "What do you like more?", "Song comparison", JOptionPane.QUESTION_MESSAGE);
        Object result = list.getSelectedValue();
        if (result == null) return compareSongs(x, y);
        System.out.printf("\"%s\"  %s  \"%s\"\n", x, result == x ? ">" : "<", y);
        return result == null ? 0 : result == x ? 1 : -1;
    }

    private static <T> List<T> search(List<T> all, String searchMessage, String searchTitle, String message, String title) {
        String result = askInput(searchMessage, searchTitle);
        if (result == null) return null;
        JList<T> list = filteredList(all, result);
        showDialog(list, message, title, JOptionPane.QUESTION_MESSAGE);
        return list.getSelectedValuesList();
    }

    private static <T> JList<T> filteredList(List<T> all, String filter) {
        String substring = filter.toLowerCase(Locale.ROOT);
        Vector<T> filtered = all.stream().filter(t -> asString(t).contains(substring))
                .sorted(Comparator.comparingInt(t -> asString(t).indexOf(substring))
                        .thenComparing(t -> -asString(t).split(substring).length)
                        .thenComparing(Object::toString)).collect(Collectors.toCollection(Vector::new));
        return new JList<>(filtered);
    }

    private static String asString(Object t) {
        return t.toString().toLowerCase(Locale.ROOT);
    }

    private static <T> void displayTop(List<T> items) {
        if (items.isEmpty()) return;
        String[] array = new String[items.size()];
        for (int i = 0; i < array.length; i++) array[i] = "%d.  %s".formatted(i + 1, items.get(i));
        showDialog(new JList<>(array), "The result top %d:".formatted(items.size()), "Result display", JOptionPane.PLAIN_MESSAGE);
    }

    private static <T> void showDialog(JList<T> list, String message, String title, int type) {
        int size = list.getModel().getSize();
        if (size == 0) return;
        list.setVisibleRowCount(Math.min(size, 10));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        Object paneMessage = message == null ? scrollPane : new Object[]{message, scrollPane};
        JOptionPane pane = new JOptionPane(paneMessage, type, JOptionPane.DEFAULT_OPTION);
        JDialog dialog = pane.createDialog(title);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                list.grabFocus();
            }
        });
        dialog.setVisible(true);
        dialog.dispose();
        Object value = pane.getValue();
        if (!(value instanceof Integer) || (int) value != JOptionPane.OK_OPTION) list.clearSelection();
    }
}