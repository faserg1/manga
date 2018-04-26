package com.danilov.supermanga.core.util;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.http.CancellableInputStream;
import com.danilov.supermanga.core.http.ICancelled;
import com.danilov.supermanga.core.http.IProgressChangeListener;
import com.danilov.supermanga.core.http.ProgressInputStream;
import com.danilov.supermanga.core.model.Manga;

import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IoUtils {
    public static final String TAG = "IoUtils";

    public static String convertStreamToString(InputStream stream) throws IOException {
        byte[] bytes = convertStreamToBytes(stream);

        return convertBytesToString(bytes);
    }

    public static byte[] convertStreamToBytes(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        copyStream(stream, output);

        return output.toByteArray();
    }

    public static void copyStream(InputStream from, OutputStream to) throws IOException {
        byte data[] = new byte[8192];
        int count;

        while ((count = from.read(data)) != -1) {
            to.write(data, 0, count);
        }

        from.close();
    }

    public static void closeStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public static String convertBytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        String result;
        try {
            result = new String(bytes, Charset.forName(HTTP.UTF_8).name());
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        return result;
    }

    public static String convertBytesToString(byte[] bytes, int len) {
        if (bytes == null) {
            return null;
        }

        String result;
        try {
            result = new String(bytes, 0, len, Charset.forName(HTTP.UTF_8).name());
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        return result;
    }

    public static String convertBytesToString(final byte[] bytes, final int offset, final int len) {
        if (bytes == null) {
            return null;
        }

        String result;
        try {
            result = new String(bytes, offset, len, Charset.forName(HTTP.UTF_8).name());
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        return result;
    }

    public static long dirSize(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        }

        long result = 0;
        for (File file : files) {
            // Recursive call if it's a directory
            if (file.isDirectory()) {
                result += dirSize(file);
            } else {
                // Sum the file size in bytes
                result += file.length();
            }
        }
        return result; // return the file size
    }

    public static void deleteDirectory(File path) {
        if (path != null && path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }

            path.delete();
        }
    }

    public static void copyArray(final byte[] from, final byte[] to) {
        copyArray(from, 0, to, 0);
    }

    public static void copyArray(final byte[] from, final int offsetFrom, final byte[] to, final int offsetTo) {
        copyArray(from, offsetFrom, from.length, to, offsetTo);
    }

    public static void copyArray(final byte[] from, final int offsetFrom, final int lenFrom, final byte[] to, final int offsetTo) {
        if (lenFrom - offsetFrom > to.length - offsetTo) {
            throw new RuntimeException("Can not copy: destination size is too small: from actual length = " + (from.length - offsetFrom)
                    + ", to actual length = " + (to.length - offsetTo));
        }
        System.arraycopy(from, offsetFrom, to, offsetTo, lenFrom - offsetFrom);
    }

    public static long freeSpace(File path, long bytesToRelease) {
        long released = 0;

        if (path != null && path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return 0;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    released += freeSpace(file, bytesToRelease);
                } else {
                    released += file.length();
                    file.delete();
                }

                if (released > bytesToRelease) {
                    break;
                }
            }
        }

        return released;
    }

    public static double getSizeInMegabytes(File folder1, File folder2) {
        long size1 = IoUtils.dirSize(folder1);
        long size2 = IoUtils.dirSize(folder2);

        double allSizeMb = convertBytesToMb(size1 + size2);

        return Math.round(allSizeMb * 100) / 100d;
    }

    public static File getSaveFilePath(Uri uri, ApplicationSettings settings) {
        String fileName = uri.getLastPathSegment();

        File dir = new File(Environment.getExternalStorageDirectory(), settings.getDownloadPath());
        dir.mkdirs();

        return new File(dir, fileName);
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaColumns.DATA;
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

        // DocumentProvider
       // if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        if (isKitKat) {
//            // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                if ("primary".equalsIgnoreCase(type)) {
//                    return Environment.getExternalStorageDirectory() + "/" + split[1];
//                }
//
//                // TODO handle non-primary volumes
//            }
//            // DownloadsProvider
//            else if (isDownloadsDocument(uri)) {
//
//                final String id = DocumentsContract.getDocumentId(uri);
//                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//
//                return getDataColumn(context, contentUri, null, null);
//            }
//            // MediaProvider
//            else if (isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                Uri contentUri = null;
//                if ("image".equals(type)) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//
//                final String selection = BaseColumns._ID + "=?";
//                final String[] selectionArgs = new String[] { split[1] };
//
//                return getDataColumn(context, contentUri, selection, selectionArgs);
//            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static InputStream modifyInputStream(InputStream stream, long contentLength, IProgressChangeListener listener, ICancelled task) throws IllegalStateException, IOException {
        if (listener != null) {
            listener.setContentLength(contentLength);

            ProgressInputStream pin = new ProgressInputStream(stream);
            pin.addProgressChangeListener(listener);

            stream = pin;
        }

        if (task != null) {
            stream = new CancellableInputStream(stream, task);
        }

        return stream;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isLocal(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            return true;
        }
        return false;
    }

    public static File getFile(Context context, Uri uri) {
        if (uri != null) {
            String path = getPath(context, uri);
            if (path != null && isLocal(path)) {
                return new File(path);
            }
        }
        return null;
    }

    public static double convertBytesToMb(long bytes) {
        return bytes / 1024d / 1024d;
    }

    public static long convertMbToBytes(double mb) {
        return (long) (mb * 1024 * 1024);
    }

    private static final Pattern urlPattern = Pattern.compile("(https?:\\/\\/)?([\\w\\.]+)\\.([a-z]{2,6}\\.?)(\\/[\\w\\-\\.]*)*\\/?");

    public static List<String> extractUrls(final String str) {
        Matcher matcher = urlPattern.matcher(str);
        List<String> urls = new ArrayList<>();
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        return urls;
    }

    private static final Pattern normalNamePattern = Pattern.compile("([\\w,.а-яА-Я])*");

    public static String createPathForManga(final Manga manga, final Context context) {
        ApplicationSettings applicationSettings = ApplicationSettings.get(context);
        ApplicationSettings.UserSettings userSettings = applicationSettings.getUserSettings();
        String downloadPath = userSettings.getDownloadPath();
        String title = manga.getTitle();
        Matcher matcher = normalNamePattern.matcher(title);
        title = "";
        while (matcher.find()) {
            title += matcher.group();
        }

        return downloadPath + File.separator + title + File.separator;
    }

    public static String createPathForURL(final String url) {
        Matcher matcher = normalNamePattern.matcher(url);
        String title = "";
        while (matcher.find()) {
            title += matcher.group();
        }
        final int length = title.length();
        if (length > 50) {
            title = title.substring(length - 50);
        }
        return title;
    }

    public static String createPathForMangaChapter(final String mangaPath, final int chapterNum) {
        File chapterFolder = new File(mangaPath + "/" + chapterNum + "/");
        if (!chapterFolder.mkdirs() && !chapterFolder.exists()) {
            Log.d(TAG, "Error while creating folder for path: " + chapterFolder.toString());
        }
        return chapterFolder.getPath();
    }

    public static String joinPath(final String firstPath, final String ... other)
	{
		//Решение с java.nio.file тут нет, пилим "велосипед"
		//https://stackoverflow.com/questions/412380/how-to-combine-paths-in-java
		File completePath = new File(firstPath);
		for (final String addonPath: other)
		{
			completePath = new File(completePath, addonPath);
		}
		return completePath.getPath();
	}
}
