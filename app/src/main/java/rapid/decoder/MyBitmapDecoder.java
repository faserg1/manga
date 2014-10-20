package rapid.decoder;


import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public abstract class MyBitmapDecoder extends BitmapDecoder {


    private static final String MESSAGE_URI_REQUIRES_CONTEXT = "This type of uri requires Context" +
            ". Use BitmapDecoder.from(Context, Uri) instead.";

    public static MyBitmapLoader myFrom(String uriOrPath, boolean useCache) {
        if (uriOrPath.contains("://")) {
            return myFrom(Uri.parse(uriOrPath), useCache);
        } else {
            return (MyBitmapLoader) new FileLoader(uriOrPath).useMemoryCache(useCache);
        }
    }

    public static MyBitmapLoader myFrom(Uri uri, boolean useCache) {
        return myFrom(null, uri, useCache);
    }

    public static MyBitmapLoader myFrom(final Context context, final Uri uri,
                                    boolean useCache) {
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            if (context == null) {
                throw new IllegalArgumentException(MESSAGE_URI_REQUIRES_CONTEXT);
            }

            Resources res;
            String packageName = uri.getAuthority();
            if (context.getPackageName().equals(packageName)) {
                res = context.getResources();
            } else {
                PackageManager pm = context.getPackageManager();
                try {
                    res = pm.getResourcesForApplication(packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    return null;
                }
            }

            int id = 0;
            List<String> segments = uri.getPathSegments();
            int size = segments.size();
            if (size == 2 && segments.get(0).equals("drawable")) {
                String resName = segments.get(1);
                id = res.getIdentifier(resName, "drawable", packageName);
            } else if (size == 1 && TextUtils.isDigitsOnly(segments.get(0))) {
                try {
                    id = Integer.parseInt(segments.get(0));
                } catch (NumberFormatException ignored) {
                }
            }

            if (id == 0) {
                return null;
            } else {
                return null;
            }
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            return (MyBitmapLoader) new FileLoader(uri.getPath()).useMemoryCache(useCache);
        } else if ("http".equals(scheme) || "https".equals(scheme) || "ftp".equals(scheme)) {
            String uriString = uri.toString();
            BitmapLoader d = null;

            synchronized (sDiskCacheLock) {
                if (useCache && sDiskCache != null) {
                    InputStream in = sDiskCache.get(uriString);
                    if (in != null) {
                        d = new StreamBitmapLoader(in);
                        d.mIsFromDiskCache = true;
                    }
                }

                if (d == null) {
                    StreamBitmapLoader sd = new StreamBitmapLoader(new LazyInputStream(new StreamOpener() {
                        @Override
                        public InputStream openInputStream() {
                            try {
                                return new URL(uri.toString()).openStream();
                            } catch (MalformedURLException e) {
                                throw new IllegalArgumentException(e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }));
                    if (useCache && sDiskCache != null) {
                        sd.setCacheOutputStream(sDiskCache.getOutputStream(uriString));
                    }
                    d = sd;
                }
            }

            d.id(uri);
            return (MyBitmapLoader) d.useMemoryCache(useCache);
        } else {
            if (context == null) {
                throw new IllegalArgumentException(MESSAGE_URI_REQUIRES_CONTEXT);
            }
            final ContentResolver cr = context.getContentResolver();
            StreamBitmapLoader d = new StreamBitmapLoader(new LazyInputStream(new StreamOpener() {

                @Override
                public InputStream openInputStream() {
                    try {
                        return cr.openInputStream(uri);
                    } catch (FileNotFoundException e) {
                        return null;
                    }
                }
            }));
            d.id(uri);
            return (MyBitmapLoader) d.useMemoryCache(useCache);
        }
    }

}