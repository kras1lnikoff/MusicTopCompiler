package com.soft.util;

import java.util.*;

public class SortedList<E> extends ArrayList<E> {

    public static <E> List<E> makeTop(int maxSize, Comparator<? super E> comparator, Collection<E> elements) {
        SortedList<E> list = new SortedList<>(maxSize, comparator.reversed());
        list.addAll(elements);
        return new ArrayList<>(list);
    }

    private final int maxSize;
    private final Comparator<? super E> comparator;

    public SortedList(int maxSize, Comparator<? super E> comparator) {
        super(maxSize < 0 ? 1 : maxSize + 1);
        this.maxSize = maxSize;
        this.comparator = comparator;
    }

    public int maxSize() {
        return maxSize;
    }

    public Comparator<? super E> comparator() {
        return comparator;
    }

    public boolean isMaxed() {
        return size() >= maxSize && maxSize >= 0;
    }

    public void ensureSorted() {
        sort(comparator);
    }

    public boolean ensureSized() {
        if (!isMaxed() || size() == maxSize) return true;
        removeRange(maxSize, size());
        return false;
    }

    public int find(E element) {
        int low = 0, high = size() - 1;
        if (high < 0 || isMaxed() && comparator.compare(get(high), element) < 0) return high + 1;
        while (low <= high) {
            int mid = low + high >> 1;
            int result = comparator.compare(get(mid), element);
            if (result == 0) return mid;
            if (result < 0) low = mid + 1;
            else high = mid - 1;
        }
        return low;
    }

    @Override
    public boolean add(E element) {
        int index = find(element), size = size();
        add(index, element);
        return ensureSized() || index != size;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean modified = false;
        for (E element : collection) modified |= add(element);
        return modified;
    }
}