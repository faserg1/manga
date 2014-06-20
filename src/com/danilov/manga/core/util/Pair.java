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

    public void retrieve() {
        clear();
        pool.add(this);
    }

    public static Pair obtain() {
        if (pool.isEmpty()) {
            return new Pair();
        }
        return pool.remove();
    }

    private void clear() {
        this.first = null;
        this.second = null;
    }

}
