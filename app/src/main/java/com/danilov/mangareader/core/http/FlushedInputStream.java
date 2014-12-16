package com.danilov.mangareader.core.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class FlushedInputStream extends FilterInputStream {
    public FlushedInputStream(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public long skip(long n) throws IOException {
        long totalBytesSkipped = 0L;
        while (totalBytesSkipped < n) {
            long bytesSkipped = this.in.skip(n - totalBytesSkipped);
            if (bytesSkipped == 0L) {
                int ibyte = this.read();
                if (ibyte < 0) {
                    break; // we reached EOF
                } else {
                    bytesSkipped = 1; // we read one byte
                }
            }
            totalBytesSkipped += bytesSkipped;
        }
        return totalBytesSkipped;
    }
}
