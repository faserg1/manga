package rapid.decoder;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.os.Build;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Semyon on 20.10.2014.
 */
public class FileLoader extends MyBitmapLoader {

    public FileLoader(String pathName) {
        if (pathName == null) {
            throw new NullPointerException();
        }
        mId = pathName;
    }

    protected FileLoader(FileLoader other) {
        super(other);
    }

    @Override
    protected Bitmap decode(BitmapFactory.Options opts) {
        return BitmapFactory.decodeFile((String) mId, opts);
    }

    @Override
    protected InputStream getInputStream() {
        try {
            return new FileInputStream((String) mId);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    protected BitmapRegionDecoder createBitmapRegionDecoder() {
        try {
            return BitmapRegionDecoder.newInstance((String) mId, false);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public MyBitmapLoader fork() {
        return new FileLoader(this);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof FileBitmapLoader && super.equals(o);
    }

}