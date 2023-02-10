package com.soft.util;

import java.util.*;

public class SizedQueue<E> extends PriorityQueue<E> {

    public static <E> List<E> makeTop(int maxSize, Comparator<? super E> comparator, Collection<E> elements) {
        SizedQueue<E> queue = new SizedQueue<>(maxSize, comparator);
        queue.addAll(elements);
        return queue.top();
    }

    private final int maxSize;

    public SizedQueue(int maxSize, Comparator<? super E> comparator) {
        super(maxSize < 0 ? 1 : maxSize + 1, comparator);
        this.maxSize = maxSize;
    }

    public int maxSize() {
        return maxSize;
    }

    public boolean isMaxed() {
        return size() >= maxSize && maxSize >= 0;
    }

    @Override
    public boolean offer(E element) {
        if (isMaxed()) {
            if (isEmpty() || comparator().compare(element, peek()) <= 0) return false;
            poll();
        }
        return super.offer(element);
    }

    public List<E> top() {
        List<E> list = new ArrayList<>(this);
        list.sort(comparator());
        Collections.reverse(list);
        return list;
    }
}