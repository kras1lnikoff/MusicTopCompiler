package com.soft;

import java.util.*;

public final class Compiler<E> {

    @FunctionalInterface
    public interface TopMaker {

        <E> List<E> makeTop(int maxSize, Comparator<? super E> comparator, Collection<E> elements);
    }

    public static double estimateComparisons(int size, int maxSize, int runs, TopMaker topMaker) {
        Random random = new Random(0);
        List<Integer> elements = new ArrayList<>(size);
        for (int index = 0; index < size; index++) elements.add(index);
        long sum = 0;
        for (int i = 0; i < runs; i++) {
            Collections.shuffle(elements, random);
            Compiler<Integer> compiler = new Compiler<>(maxSize, Integer::compareTo, elements, topMaker);
            compiler.compile();
            sum += compiler.comparisons();
        }
        return  (double) sum / runs;
    }

    private final TopMaker topMaker;

    private final int maxSize;
    private final Comparator<? super E> comparator;
    private final Collection<E> elements;

    private int comparisons;
    private Map<Integer, Map<E, Set<E>>> cachedResults;

    public Compiler(int maxSize, Comparator<? super E> comparator, Collection<E> elements, TopMaker topMaker) {
        this.elements = elements;
        this.maxSize = maxSize;
        this.comparator = comparator;
        this.topMaker = topMaker;
    }

    public int comparisons() {
        return comparisons;
    }

    public double estimate(int runs) {
        return estimateComparisons(elements.size(), maxSize, runs, topMaker);
    }

    public List<E> compile() {
        comparisons = 0;
        initCache();
        return topMaker.makeTop(maxSize, this::compare, elements);
    }

    private int compare(E first, E second) {
        Integer result = getFromCache(first, second);
        if (result != null) return result;
        result = Integer.signum(comparator.compare(first, second));
        comparisons++;
        updateCache(first, second, result);
        return result;
    }

    private void initCache() {
        cachedResults = new HashMap<>();
        for (int result = -1; result <= 1; result++) cachedResults.put(result, new HashMap<>());
        for (E element : elements) cacheResult(element, element, 0);
    }

    private Set<E> listCached(E element, int result) {
        return cachedResults.get(result).computeIfAbsent(element, e -> new HashSet<>());
    }

    private void cacheResult(E first, E second, int result) {
        listCached(first, result).add(second);
        listCached(second, -result).add(first);
    }

    private Integer getFromCache(E first, E second) {
        for (int result = -1; result <= 1; result++) if (listCached(first, result).contains(second)) return result;
        return null;
    }

    private void updateCache(E first, E second, int result) {
        cacheResult(first, second, result);
        for (E third : listCached(second, result)) cacheResult(first, third, result);
        for (E third : listCached(first, -result)) cacheResult(third, second, result);
    }
}