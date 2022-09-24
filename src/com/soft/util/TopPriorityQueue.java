package com.soft.util;

import java.util.*;
import java.util.function.Predicate;

public class TopPriorityQueue<E> extends PriorityQueue<E> {

    public static <E> TopPriorityQueue<E> from(Collection<? extends E> collection, Comparator<? super E> comparator, int maxSize) {
        return from(collection, comparator, maxSize, null);
    }

    public static <E> TopPriorityQueue<E> from(Collection<? extends E> collection,
                                               Comparator<? super E> comparator, int maxSize, Predicate<? super E> filter) {
        TopPriorityQueue<E> queue = new TopPriorityQueue<>(comparator, maxSize);
        if (filter == null) queue.addAll(collection);
        else for (E e : collection) if (filter.test(e)) queue.add(e);
        return queue;
    }

    private final int maxSize;

    public TopPriorityQueue(Comparator<? super E> comparator, int maxSize) {
        super(maxSize <= 0 ? 1 : maxSize, comparator);
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public boolean offer(E e) {
        if (maxSize < 0 || size() < maxSize) return super.offer(e);
        if (maxSize == 0 || comparator().compare(e, peek()) <= 0) return false;
        poll();
        return super.offer(e);
    }

    public List<E> sorted() {
        List<E> list = new ArrayList<>(this);
        list.sort(comparator());
        return list;
    }
}