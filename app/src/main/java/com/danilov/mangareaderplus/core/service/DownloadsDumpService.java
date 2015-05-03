package com.danilov.mangareaderplus.core.service;

import android.os.Environment;

import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.List;

/**
 * Created by Semyon on 22.04.2015.
 */
public class DownloadsDumpService {

    private static final String DOWNLOADS = "downloads";

    public void dumpDownloads(final List<MangaDownloadService.MangaDownloadRequest> requests, final List<DownloadManager.Download> downloads) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < requests.size(); i++) {
                MangaDownloadService.MangaDownloadRequest request = requests.get(i);
                JSONObject requestJSON = requestToJSON(request);
                if (i == 0) {
                    JSONArray downloadsArray = new JSONArray();
                    for (DownloadManager.Download download : downloads) {
                        downloadsArray.put(downloadToJSON(download));
                    }
                    requestJSON.put(DOWNLOADS, downloadsArray);
                }
                jsonArray.put(requestJSON);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/dump-temp.txt", false);
            fileWriter.write(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void unDump() {
        StringBuilder fileData = new StringBuilder();
        try {
            String path = Environment.getExternalStorageDirectory() + "/dump-temp.txt";
            Reader fileReader = new InputStreamReader(new FileInputStream(path), "UTF-8");
            //System.out.println(reader.getEncoding());
            BufferedReader reader = new BufferedReader(fileReader);
            char[] buf = new char[1024];
            int numRead = 0;
            while((numRead = reader.read(buf)) != -1){
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
        } catch (Exception e) {

        }
        String data = fileData.toString();
        try {
            JSONArray array = new JSONArray(data);
            array.isNull(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static final String CURRENT_CHAPTER_IN_LIST = "CURRENT_CHAPTER_IN_LIST";
    private static final String CURRENT_CHAPTER_NUMBER = "CURRENT_CHAPTER_NUMBER";
    private static final String CURRENT_CHAPTER = "CURRENT_CHAPTER";
    private static final String MANGA = "MANGA";
    private static final String QUANTITY = "QUANTITY";
    private static final String WHICH_CHAPTERS = "WHICH_CHAPTERS";

    private JSONObject requestToJSON(final MangaDownloadService.MangaDownloadRequest request) throws JSONException{
        JSONObject jsonObject = new JSONObject();

        int currentChapterInList = request.getCurrentChapterInList();
        jsonObject.put(CURRENT_CHAPTER_IN_LIST, currentChapterInList);

        int currentChapterNumber = request.getCurrentChapterNumber();
        jsonObject.put(CURRENT_CHAPTER_NUMBER, currentChapterNumber);

        MangaChapter currentChapter = request.getCurrentChapter();
        jsonObject.put(CURRENT_CHAPTER, mangaChapterToJSON(currentChapter));

        Manga manga = request.getManga();
        jsonObject.put(MANGA, mangaToJSON(manga));

        int quantity = request.getQuantity();
        jsonObject.put(QUANTITY, quantity);

        List<Integer> whichChapters = request.getWhichChapters();
        jsonObject.put(WHICH_CHAPTERS, whichChapters);

        return jsonObject;
    }

    private static final String URI = "URI";
    private static final String SIZE = "SIZE";
    private static final String STATUS = "STATUS";
    private static final String FILE_PATH = "FILE_PATH";
    private static final String DOWNLOADED = "DOWNLOADED";
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    private JSONObject downloadToJSON(final DownloadManager.Download download) throws JSONException{
        JSONObject jsonObject = new JSONObject();

        String uri = download.getUri();
        jsonObject.put(URI, uri);

        int size = download.getSize();
        jsonObject.put(SIZE, size);

        DownloadManager.DownloadStatus status = download.getStatus();
        jsonObject.put(STATUS, status);

        String filePath = download.getFilePath();
        jsonObject.put(FILE_PATH, filePath);

        int downloaded = download.getDownloaded();
        jsonObject.put(DOWNLOADED, downloaded);

        String errorMessage = download.getErrorMessage();
        jsonObject.put(ERROR_MESSAGE, errorMessage);

        return jsonObject;
    }

    private static final String COVER_URI = "COVER_URI";
    private static final String AUTHOR = "AUTHOR";
    private static final String CHAPTERS_QUANTITY = "CHAPTERS_QUANTITY";
    private static final String CHAPTERS = "CHAPTERS";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String ID = "ID";
    private static final String REPOSITORY = "REPOSITORY";
    private static final String TITLE = "TITLE";

    private JSONObject mangaToJSON(final Manga manga) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        String uri = manga.getUri();
        jsonObject.put(URI, uri);

        String coverUri = manga.getCoverUri();
        jsonObject.put(COVER_URI, coverUri);

        String author = manga.getAuthor();
        jsonObject.put(AUTHOR, author);

        int chaptersQuantity = manga.getChaptersQuantity();
        jsonObject.put(CHAPTERS_QUANTITY, chaptersQuantity);

        String description = manga.getDescription();
        jsonObject.put(DESCRIPTION, description);

        JSONArray chapters = new JSONArray();
        for (MangaChapter chapter : manga.getChapters()) {
            chapters.put(mangaChapterToJSON(chapter));
        }
        jsonObject.put(CHAPTERS, chapters);

        int id = manga.getId();
        jsonObject.put(ID, id);

        String repository = manga.getRepository().toString();
        jsonObject.put(REPOSITORY, repository);

        String title = manga.getTitle();
        jsonObject.put(TITLE, title);

        return jsonObject;
    }

    private static final String NUMBER = "NUMBER";

    private JSONObject mangaChapterToJSON(final MangaChapter chapter) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        String uri = chapter.getUri();
        jsonObject.put(URI, uri);

        int number = chapter.getNumber();
        jsonObject.put(NUMBER, number);

        String title = chapter.getTitle();
        jsonObject.put(TITLE, title);

        return jsonObject;
    }

}
