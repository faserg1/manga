package com.danilov.manga.core.util;


import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by Semyon Danilov on 14.06.2014.
 */
public class Pair {

    private static Queue<Pair> pool = new ArrayDeque<Pair>(50);

    public Object first;

    public Object second;

    private Pair() {
    }

    private Pair(final Object first, final Object second) {
        this.first = first;
        this.second = second;
    }

    public void retrieve() {
        clear();
        pool.add(this);
    }

    public static Pair obtain() {
        return obtain(null, null);
    }

    public static Pair obtain(final Object first, final Object second) {
        if (pool.isEmpty()) {
            return new Pair(first, second);
        }
        Pair pair = pool.remove();
        pair.first = first;
        pair.second = second;
        return pair;
    }

    private void clear() {
        this.first = null;
        this.second = null;
    }

}
