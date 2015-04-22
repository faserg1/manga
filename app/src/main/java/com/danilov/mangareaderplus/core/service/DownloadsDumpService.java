package com.danilov.mangareaderplus.core.service;

import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.MangaChapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        System.out.println(jsonArray.toString());

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
        jsonObject.put(CURRENT_CHAPTER, currentChapter);

        Manga manga = request.getManga();
        jsonObject.put(MANGA, manga);

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

}
