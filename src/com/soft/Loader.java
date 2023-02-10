package com.soft;

import com.soft.data.*;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class Loader {

    private final Storage storage;

    private final Map<String, Integer> allFound = new HashMap<>(), loaded = new HashMap<>();

    public Loader(Storage storage) {
        this.storage = storage;
    }

    public void loadStorage() {
        loadUI(storage::init);
        System.out.printf("%d tracks, %d albums, %d artists were found.\n",
                storage.tracks().size(), storage.albums().size(), storage.artists().size());
    }

    public void loadUser(User user) {
        loadAll(user.getPlaylists());
        loadAll(user.getArtists());
        loadAll(user.getAlbums());
        loadAll(user.getTracks());
        user.dispose();
    }

    public void loadArtist(Artist artist) {
        loadAll(artist.getAlbums());
        loadAll(artist.getTracks());
        artist.dispose();
    }

    public void loadAll(Collection<? extends Entry> entries) {
        loadUI(() -> entries.parallelStream().forEach(this::load));
    }

    public void load(Entry entry) {
        boolean alreadyLoaded = entry.isLoaded();
        entry.load();
        System.out.printf("- %s %s: \"%s\"\n", entry.type(), alreadyLoaded ? "found" : "loaded", entry);
        entry.dispose();
        allFound.merge(entry.type(), 1, Integer::sum);
        if (!alreadyLoaded) loaded.merge(entry.type(), 1, Integer::sum);
    }

    private void loadUI(Runnable action) {
        allFound.clear();
        loaded.clear();
        JLabel label = new JLabel(" ".repeat(200));
        Thread display = new Thread(() -> Display.info(label, "Loading information"));
        display.start();
        long start = System.currentTimeMillis();
        Thread loading = new Thread(action);
        loading.start();
        while (loading.isAlive()) {
            String info = buildInfo(start);
            label.setText(info + " ".repeat(50));
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }
        }
        display.interrupt();
        System.out.println(buildInfo(start));
    }

    private String buildInfo(long start) {
        String time = " in %.2f seconds.".formatted((System.currentTimeMillis() - start) * 1e-3);
        if (allFound.isEmpty()) return "Loaded" + time;
        return allFound.entrySet().stream().map(entry -> {
            String type = entry.getKey();
            int all = entry.getValue(), nw = loaded.getOrDefault(type, 0);
            return " %d%s %ss".formatted(all, nw == 0 ? "" : " (loaded " + nw + ")", type);
        }).collect(Collectors.joining(",", "Found", time));
    }
}