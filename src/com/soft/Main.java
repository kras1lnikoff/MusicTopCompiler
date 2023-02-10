package com.soft;

import com.soft.data.Track;
import com.soft.util.SortedList;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Main {

    public static void main(String[] args) {
        while (true) compile();
    }

    public static void compile() {
        new Main().run();
    }

    private Compiler<Track> compiler;
    private int estimate;

    private void run() {
        System.out.println("Preparing tracks...");
        List<Track> tracks = Selector.selectTracks();
        System.out.printf("Preparation finished. %d tracks were selected.\n", tracks.size());
        int size = tracks.size(), maxSize = askMaxSize();
        if (maxSize == 0 || size == 0) return;
        if (maxSize < 0 || maxSize > size) maxSize = size;
        compiler = new Compiler<>(maxSize, this::askCompare, tracks, SortedList::makeTop);
        estimate = (int) Math.round(estimateComparisons(size, maxSize));
        System.out.printf("Top will consist of %d tracks. Estimated number of comparisons: %d.\n", maxSize, estimate);
        System.out.println("Compiling tracks...");
        List<Track> top = compiler.compile();
        System.out.printf("Compilation finished. %d comparisons were made.\n", compiler.comparisons());
        displayTop(top);
        System.out.println();
    }

    private double estimateComparisons(int size, int maxSize) {
        if (size < 25) return compiler.estimate(1000);
        if (size < 50) return compiler.estimate(250);
        if (size < 100) return compiler.estimate(75);
        if (size < 250) return compiler.estimate(25);
        if (size < 500) return compiler.estimate(5);
        int estimated = size - 1 + (size - maxSize) * log2(maxSize) / 2;
        for (int i = 1; i < maxSize; i++) estimated += log2(i);
        return estimated;
    }

    private int log2(int n) {
        return (int) (Math.log(n) / Math.log(2));
    }

    private int askCompare(Track x, Track y) {
        JList<Track> list = Selector.buildList(Arrays.asList(x, y), false);
        Track selected = Display.single(Display.select(list,
                "What do you like more?", "Track comparison", () -> null));
        if (selected == null) return askCompare(x, y);
        int number = compiler.comparisons() + 1, percent = Math.min(99, 100 * number / estimate);
        System.out.printf("%d. (~%d%%)  \"%s\"  %s  \"%s\"\n", number, percent, x, selected == x ? ">" : "<", y);
        return selected == x ? 1 : -1;
    }

    private static int askMaxSize() {
        try {
            return Integer.parseInt(Display.ask("Type the maximum top size:", "Final configuration"));
        } catch (Exception e) {
            return askMaxSize();
        }
    }

    private static void displayTop(List<Track> top) {
        if (top.isEmpty()) return;
        List<String> formattedTop = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) formattedTop.add("%d.  %s".formatted(i + 1, top.get(i)));
        System.out.printf("The result top %d:\n", top.size());
        for (String item : formattedTop) System.out.println(item);
        Display.list(formattedTop, "The result top %d:".formatted(top.size()), "Result display");
    }
}