package com.danilov.manga.core.repository;

import android.util.Log;
import com.danilov.manga.core.http.*;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import com.danilov.manga.core.util.IoUtils;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.util.Utils;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Semyon Danilov on 17.05.2014.
 */

/**
 * Engine for russian most popular manga site, geh
 */
public class ReadmangaEngine implements RepositoryEngine {

    private static final String TAG = "ReadmangaEngine";

    private String baseSearchUri = "http://readmanga.me/search?q=";
    public static final String baseUri = "http://readmanga.me";

    @Override
    public String getLanguage() {
        return "Русский";
    }

    @Override
    public JSONObject getSuggestions(final String query) {
        return null;
    }

    @Override
    public List<Manga> queryRepository(final String query) {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        List<Manga> mangaList = null;
        if (httpBytesReader != null) {
            try {
                String uri = baseSearchUri + URLEncoder.encode(query, Charset.forName(HTTP.UTF_8).name());
                byte[] response = httpBytesReader.fromUri(uri);
                String responseString = IoUtils.convertBytesToString(response);
                mangaList = parseSearchResponse(Utils.parseForDocument(responseString));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (HttpRequestException e) {
                e.printStackTrace();
            }
        }
        return mangaList;
    }

    @Override
    public boolean queryForMangaDescription(final Manga manga) throws RepositoryException {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        if (httpBytesReader != null) {
            String uri = baseUri + manga.getUri();
            byte[] response = null;
            try {
                response = httpBytesReader.fromUri(uri);
            } catch (HttpRequestException e) {
                Log.d(TAG, e.getMessage());
                throw new RepositoryException(e.getMessage());
            }
            String responseString = IoUtils.convertBytesToString(response);
            String mangaDescription = parseMangaDescriptionResponse(Utils.parseForDocument(responseString));
            if (mangaDescription == null) {
                return false;
            }
            manga.setDescription(mangaDescription);
            return true;
        }
        return false;
    }

    @Override
    public boolean queryForChapters(final Manga manga) throws RepositoryException {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        if (httpBytesReader != null) {
            String uri = baseUri + manga.getUri();
            byte[] response = null;
            try {
                response = httpBytesReader.fromUri(uri);
            } catch (HttpRequestException e) {
                Log.d(TAG, e.getMessage());
                throw new RepositoryException(e.getMessage());
            }
            String responseString = IoUtils.convertBytesToString(response);
            List<MangaChapter> chapters = parseMangaChaptersResponse(Utils.parseForDocument(responseString));
            if (chapters != null) {
                manga.setChapters(chapters);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getChapterImages(final MangaChapter chapter) throws RepositoryException {
        String uri = baseUri + chapter.getUri() + "?mature=1";
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        HttpStreamReader httpStreamReader = ServiceContainer.getService(HttpStreamReader.class);
        byte[] bytes = new byte[1024];
        List<String> imageUrls = null;
        LinesSearchInputStream inputStream = null;
        try {
            HttpStreamModel model = httpStreamReader.fromUri(uri);
            inputStream = new LinesSearchInputStream(model.stream, "pictures = [", "];");
            int status = LinesSearchInputStream.SEARCHING;
            while (status == LinesSearchInputStream.SEARCHING) {
                status = inputStream.read(bytes);
            }
            bytes = inputStream.getResult();
            String str = IoUtils.convertBytesToString(bytes);
            imageUrls = extractUrls(str);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            throw new RepositoryException(e.getMessage());
        } catch (HttpRequestException e) {
            Log.d(TAG, e.getMessage());
            throw new RepositoryException(e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
        return imageUrls;
    }

    private final Pattern urlPattern = Pattern.compile("url:\"(.*?)\"");

    private List<String> extractUrls(final String str) {
        Log.d(TAG, "a: " + str);
        Matcher matcher = urlPattern.matcher(str);
        List<String> urls = new ArrayList<String>();
        while (matcher.find()) {
            String url = matcher.group(1);
            if (!url.contains("http")) {
                url = baseUri + url;
            }
            urls.add(url);
        }
        return urls;
    }

    //html values
    private String searchElementId = "mangaResults";
    private String mangaLinkClass = "manga-link";
    private String mangaCoverClass = "screenshot";
    private String mangaCoverLinkAttrName = "rel";

    //!html values

    private List<Manga> parseSearchResponse(final Document document) {
        List<Manga> mangaList = new LinkedList<Manga>();
        Element searchResults = document.getElementById(searchElementId);
        List<Element> mangaLinks = searchResults.getElementsByClass(mangaLinkClass);
        for (Element mangaLink : mangaLinks) {
            Element parent = mangaLink.parent();
            String uri = mangaLink.attr("href");
            String mangaName = String.valueOf(mangaLink.text());
            Element screenElement = parent.getElementsByClass(mangaCoverClass).get(0);
            String coverUri = screenElement != null ? screenElement.attr(mangaCoverLinkAttrName) : null;
            Manga manga = new Manga(mangaName, uri, Repository.READMANGA);
            manga.setCoverUri(coverUri);
            mangaList.add(manga);
        }
        return mangaList;
    }

    //html values
    private String descriptionElementClass = "manga-description";

    private String parseMangaDescriptionResponse(final Document document) {
        Elements mangaDescriptionElements = document.getElementsByClass(descriptionElementClass);
        if (mangaDescriptionElements.isEmpty()) {
            return null;
        }
        Element mangaDescription = mangaDescriptionElements.first();
        Elements links = mangaDescription.getElementsByTag("a");
        if (!links.isEmpty()) {
            links.remove();
        }
        String description = mangaDescription.text();
        return description;
    }

    //html values
    private String chaptersElementClass = "chapters-link";
    private String linkValueAttr = "href";

    private List<MangaChapter> parseMangaChaptersResponse(final Document document) {
        Elements chaptersElements = document.getElementsByClass(chaptersElementClass);
        if (chaptersElements.isEmpty()) {
            return null;
        }
        Element chaptersElement = chaptersElements.first();
        Elements links = chaptersElement.getElementsByTag("a");
        if (links.isEmpty()) {
            return null;
        }
        List<MangaChapter> chapters = new ArrayList<MangaChapter>(links.size());
        int number = 0;
        for (int i = links.size() - 1; i >= 0; i--) {
            Element element = links.get(i);
            String link = element.attr(linkValueAttr);
            String title = element.text();
            MangaChapter chapter = new MangaChapter(title, number, link);
            chapters.add(chapter);
            number++;
        }
        return chapters;
    }

    @Override
    public String getBaseSearchUri() {
        return baseSearchUri;
    }

    @Override
    public String getBaseUri() {
        return baseUri;
    }

}
