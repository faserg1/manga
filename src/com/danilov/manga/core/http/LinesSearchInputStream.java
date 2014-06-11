package com.danilov.manga.core.http;

import com.danilov.manga.core.util.IoUtils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Semyon Danilov on 11.06.2014.
 */
public class LinesSearchInputStream extends FilterInputStream {

    public static final String TAG = "ProgressInputStream";

    public static final int ERROR = -1;
    public static final int SEARCHING = 0;
    public static final int FOUND = 1;
    public static final int NOT_FOUND = 2;

    private int state = SEARCHING;

    private byte[] prevLoaded;

    private byte[] desire;

    private String lastSymbols;

    private boolean hasFoundDesired = false;

    private InputStream inputStream;

    @Override
    public int read(byte[] b) throws IOException {
        updateSearch(in.read(b), b);
        return state;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        updateSearch(in.read(b, off, len), b);
        return state;
    }

    private int successMatched = 0;

    private void updateSearch(final int red, final byte[] bytes) {
        boolean fullFound = false;
        int offset = -1;
        for (int i = 0; i < red; i++) {
            byte cur = bytes[i];
            boolean wrong = false;
            int a = i;
            for (int j = successMatched; j < desire.length; j++) {
                if (cur != desire[j]) {
                    wrong = true;
                    break;
                }
                a++;
                if (j == desire.length - 1) {
                    fullFound = true;
                }
                if (a < red) {
                    cur = bytes[a];
                } else {
                    successMatched = j;
                    break;
                }
            }
            if (wrong) {
                successMatched = 0;
            }
            if (fullFound) {
                offset = a;
                break;
            }
        }
        if (fullFound) {
            hasFoundDesired = true;
            prevLoaded = new byte[bytes.length - offset];
            IoUtils.copyArray(bytes, offset, prevLoaded, 0);
        }
    }

    private void update

    protected LinesSearchInputStream(final InputStream in) {
        super(in);
        inputStream = in;
    }

}
