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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Semyon on 22.04.2015.
 */
public class DownloadsDumpService {

    private static final String DOWNLOADS = "downloads";
    private static final String CURRENT_IMAGE = "CURRENT_IMAGE";
    private static final String CURRENT_IMAGE_QUANTITY = "CURRENT_IMAGE_QUANTITY";

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
                    requestJSON.put(CURRENT_IMAGE, request.getCurrentImage());
                    requestJSON.put(CURRENT_IMAGE_QUANTITY, request.getCurrentImageQuantity());
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

    public void unDump(final MangaDownloadService downloadService) {
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
        List<DownloadManager.Download> downloads = new ArrayList<>();
        List<MangaDownloadService.MangaDownloadRequest> requests = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                MangaDownloadService.MangaDownloadRequest request = downloadService.obtainRequest();
                jsonToRequest(jsonObject, request);
                requests.add(request);
                if (!jsonObject.isNull(DOWNLOADS)) {
                    JSONArray jsonArray = jsonObject.getJSONArray(DOWNLOADS);
                    if (jsonArray != null) {
                        for (int j = 0; j < jsonArray.length(); j++) {
                            DownloadManager.Download download = downloadService.obtainDownload();
                            jsonToDownload(jsonArray.getJSONObject(j), download);
                            downloads.add(download);
                        }
                    }
                }

            }
            downloadService.restore(requests, downloads);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static final String CURRENT_CHAPTER_IN_LIST = "CURRENT_CHAPTER_IN_LIST";
    private static final String MANGA = "MANGA";
    private static final String QUANTITY = "QUANTITY";
    private static final String WHICH_CHAPTERS = "WHICH_CHAPTERS";

    private JSONObject requestToJSON(final MangaDownloadService.MangaDownloadRequest request) throws JSONException{
        JSONObject jsonObject = new JSONObject();

        int currentChapterInList = request.getCurrentChapterInList();
        jsonObject.put(CURRENT_CHAPTER_IN_LIST, currentChapterInList);

        Manga manga = request.getManga();
        jsonObject.put(MANGA, mangaToJSON(manga));

        int quantity = request.getQuantity();
        jsonObject.put(QUANTITY, quantity);

        List<Integer> whichChapters = request.getWhichChapters();

        JSONArray jsonArray = new JSONArray();
        for (Integer i : whichChapters) {
            jsonArray.put(i.intValue());
        }
        jsonObject.put(WHICH_CHAPTERS, jsonArray);

        return jsonObject;
    }

    private void jsonToRequest(final JSONObject jsonObject, final MangaDownloadService.MangaDownloadRequest request) throws JSONException {
        int currentChapterInList = jsonObject.getInt(CURRENT_CHAPTER_IN_LIST);
        Manga manga = jsonToManga(jsonObject.getJSONObject(MANGA));
        int quantity = jsonObject.getInt(QUANTITY);
        JSONArray array = jsonObject.getJSONArray(WHICH_CHAPTERS);

        List<Integer> whichChapters = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            whichChapters.add(array.getInt(i));
        }

        int currentImage = 0;
        if (!jsonObject.isNull(CURRENT_IMAGE)) {
            currentImage = jsonObject.getInt(CURRENT_IMAGE);
        }
        int currentImageQuantity = 0;
        if (!jsonObject.isNull(CURRENT_IMAGE_QUANTITY)) {
            currentImageQuantity = jsonObject.getInt(CURRENT_IMAGE_QUANTITY);
        }
        request.setCurrentImage(currentImage);
        request.setCurrentImageQuantity(currentImageQuantity);
        request.setCurrentChapterInList(currentChapterInList);
        request.setWhichChapters(whichChapters);
        request.setQuantity(quantity);
        request.setManga(manga);
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

    private void jsonToDownload(final JSONObject jsonObject, final DownloadManager.Download download) throws JSONException {
        String uri = jsonObject.getString(URI);
        int size = jsonObject.getInt(SIZE);
        DownloadManager.DownloadStatus status = DownloadManager.DownloadStatus.valueOf(jsonObject.getString(STATUS));
        String filePath = jsonObject.getString(FILE_PATH);
        int downloaded = jsonObject.getInt(DOWNLOADED);

        if (!jsonObject.isNull(ERROR_MESSAGE)) {
            String errorMessage = jsonObject.getString(ERROR_MESSAGE);
            download.setErrorMessage(errorMessage);
        }

        download.setUri(uri);
        download.setSize(size);
        download.setStatus(status);
        download.setFilePath(filePath);
        download.setDownloaded(downloaded);
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

    private Manga jsonToManga(final JSONObject jsonObject) throws JSONException {

        String uri =  jsonObject.getString(URI);
        String coverUri = jsonObject.getString(COVER_URI);
        String author = jsonObject.getString(AUTHOR);

        int chaptersQuantity = jsonObject.getInt(CHAPTERS_QUANTITY);

        String description = jsonObject.getString(DESCRIPTION);

        JSONArray chapters = jsonObject.getJSONArray(CHAPTERS);
        List<MangaChapter> mangaChapters = new ArrayList<>(chapters.length());
        for (int i = 0; i < chapters.length(); i++) {
            JSONObject chapter = chapters.getJSONObject(i);
            mangaChapters.add(jsonToMangaChapter(chapter));
        }

        int id = jsonObject.getInt(ID);

        RepositoryEngine.Repository repository = RepositoryEngine.Repository.valueOf(jsonObject.getString(REPOSITORY));

        String title = jsonObject.getString(TITLE);

        Manga manga = new Manga(title, uri, repository);
        manga.setCoverUri(coverUri);
        manga.setAuthor(author);
        manga.setChaptersQuantity(chaptersQuantity);
        manga.setDescription(description);
        manga.setId(id);
        manga.setChapters(mangaChapters);

        return manga;
    }

    private MangaChapter jsonToMangaChapter(final JSONObject jsonObject) throws JSONException {
        String uri = jsonObject.getString(URI);
        int number = jsonObject.getInt(NUMBER);
        String title = jsonObject.getString(TITLE);
        MangaChapter mangaChapter = new MangaChapter(title, number, uri);
        return mangaChapter;
    }

}
