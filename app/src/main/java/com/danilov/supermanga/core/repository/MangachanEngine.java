package com.danilov.supermanga.core.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danilov.supermanga.core.http.ExtendedHttpClient;
import com.danilov.supermanga.core.http.HttpBytesReader;
import com.danilov.supermanga.core.http.HttpRequestException;
import com.danilov.supermanga.core.http.HttpStreamModel;
import com.danilov.supermanga.core.http.HttpStreamReader;
import com.danilov.supermanga.core.http.LinesSearchInputStream;
import com.danilov.supermanga.core.http.RequestPreprocessor;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.MangaSuggestion;
import com.danilov.supermanga.core.util.IoUtils;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Semyon on 07.01.2016.
 */
public class MangachanEngine implements RepositoryEngine {

    private static final String TAG = "MangachanEngine";

    private String baseUri = "http://mangachan.ru/";
    private String baseSuggestionUri = "http://mangachan.ru/engine/ajax/search.php";
    private String baseSearchUri = "http://mangachan.ru/?do=search&subaction=search&story=";

    @Override
    public String getLanguage() {
        return "Русский";
    }

    @Override
    public boolean requiresAuth() {
        return false;
    }

    private String suggestionPattern = "a href=\"(.*?)\"><span.*?>(.*?)</span></a>";

    @Override
    public List<MangaSuggestion> getSuggestions(final String query) throws RepositoryException {
        List<MangaSuggestion> mangaSuggestions = new ArrayList<>();
        try {
            HttpPost request = new HttpPost(baseSuggestionUri);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("query", query));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpClient httpClient = new ExtendedHttpClient();
            HttpResponse response = httpClient.execute(request);
            byte[] result = IoUtils.convertStreamToBytes(response.getEntity().getContent());
            String responseString = IoUtils.convertBytesToString(result);

            Pattern p = Pattern.compile(suggestionPattern);
            Matcher m = p.matcher(responseString);
            while (m.find()) {
                String link = m.group(1);
                String title = m.group(2);
                MangaSuggestion suggestion = new MangaSuggestion(title, link, DefaultRepository.MANGACHAN);
                mangaSuggestions.add(suggestion);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mangaSuggestions;
    }

    @Override
    public List<Manga> queryRepository(final String query, final List<Filter.FilterValue> filterValues) throws RepositoryException {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        List<Manga> mangaList = null;
        if (httpBytesReader != null) {
            try {
                String uri = baseSearchUri + URLEncoder.encode(query, Charset.forName(HTTP.UTF_8).name());
                uri += "&genre=";
                for (Filter.FilterValue filterValue : filterValues) {
                    uri = filterValue.apply(uri);
                }
                uri += "&order=2";
                byte[] response = httpBytesReader.fromUri(uri);
                String responseString = IoUtils.convertBytesToString(response);
                mangaList = parseMangaSearchResponse(Utils.toDocument(responseString));
            } catch (UnsupportedEncodingException e) {
                throw new RepositoryException("Failed to load: " + e.getMessage());
            } catch (HttpRequestException e) {
                throw new RepositoryException("Failed to load: " + e.getMessage());
            }
        }
        return mangaList;
    }

    private List<Manga> parseMangaSearchResponse(final Document document) {
        Elements mangaResults = document.select("#dle-content .content_row");

        List<Manga> mangas = new ArrayList<>();
        for (Element mangaResult : mangaResults) {
            Element nameElement = mangaResult.select(".manga_row1 h2").get(0);
            String title = nameElement.text();
            String url = nameElement.child(0).attr("href");

            Element backgroundHolder = mangaResult.getElementsByClass("manga_images").get(0);
            String imageUrl = baseUri + backgroundHolder.getElementsByTag("img").attr("src");

            Manga manga = new Manga(title, url, DefaultRepository.MANGACHAN);
            manga.setCoverUri(imageUrl);
            mangas.add(manga);
        }

        return mangas;
    }

    @Override
    public List<Manga> queryRepository(final Genre genre) throws RepositoryException {
        return null;
    }

    @Override
    public boolean queryForMangaDescription(final Manga manga) throws RepositoryException {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        if (httpBytesReader != null) {
            String uri = manga.getUri();
            byte[] response = null;
            try {
                response = httpBytesReader.fromUri(uri);
            } catch (HttpRequestException e) {
                if (e.getMessage() != null) {
                    Log.d(TAG, e.getMessage());
                } else {
                    Log.d(TAG, "Failed to load manga description");
                }
                throw new RepositoryException(e.getMessage());
            }
            String responseString = IoUtils.convertBytesToString(response);
            return parseMangaDescriptionResponse(manga, Utils.toDocument(responseString));
        }
        return false;
    }

    private boolean parseMangaDescriptionResponse(final Manga manga, final Document document) {
        String description = document.getElementById("description").text();
        manga.setDescription(description);

        if (manga.getCoverUri() == null || manga.getCoverUri().length() <= 0) {
            Element img = document.getElementById("cover");
            manga.setCoverUri(baseUri + img.attr("src"));
        }

        Element mangaDescriptionBlock = document.getElementsByClass("mangatitle").first().child(0);

        String chaptersQuantityString = mangaDescriptionBlock.child(4).select("b").text();
        try {
            int idx = chaptersQuantityString.indexOf(' ');
            if (idx != -1) {
                chaptersQuantityString = chaptersQuantityString.substring(0, idx);
            }
            Integer chaptersQuantity = Integer.valueOf(chaptersQuantityString);
            manga.setChaptersQuantity(chaptersQuantity);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse number: " + e.getMessage());
            return false;
        }

        Element genresElement = mangaDescriptionBlock.child(5).getElementsByClass("item2").first();
        manga.setGenres(genresElement.text());

        Element authorsList = mangaDescriptionBlock.child(2).getElementsByClass("item2").first();
        manga.setAuthor(authorsList.text());

        return true;
    }

    @Override
    public boolean queryForChapters(final Manga manga) throws RepositoryException {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        if (httpBytesReader != null) {
            String uri = manga.getUri();
            byte[] response = null;
            try {
                response = httpBytesReader.fromUri(uri);
            } catch (HttpRequestException e) {
                if (e.getMessage() != null) {
                    Log.d(TAG, e.getMessage());
                } else {
                    Log.d(TAG, "Failed to load manga description");
                }
                throw new RepositoryException(e.getMessage());
            }
            String responseString = IoUtils.convertBytesToString(response);
            List<MangaChapter> chapters = parseMangaChaptersResponse(manga, Utils.toDocument(responseString));
            manga.setChapters(chapters);
            return true;
        }
        return false;
    }

    private List<MangaChapter> parseMangaChaptersResponse(final Manga manga, final Document document) {
        Elements chaptersTables = document.getElementsByClass("table_cha");

        List<Element> elements = new ArrayList<>();

        for (Element element : chaptersTables) {
            Elements chaptersElements = element.getElementsByTag("a");
            for (Element chapterElement : chaptersElements) {
                elements.add(chapterElement);
            }
        }

        List<MangaChapter> chapters = new ArrayList<MangaChapter>(elements.size());

        Collections.reverse(elements);
        int number = 0;
        for (Element chapterElement : elements) {
            String link = baseUri + chapterElement.attr("href");
            String title = chapterElement.text();
            MangaChapter chapter = new MangaChapter(title, number, link);
            chapters.add(chapter);
            number++;
        }
        manga.setChaptersQuantity(chapters.size());
        return chapters;
    }

    @Override
    public List<String> getChapterImages(final MangaChapter chapter) throws RepositoryException {
        String uri = chapter.getUri();
        HttpStreamReader httpStreamReader = ServiceContainer.getService(HttpStreamReader.class);
        byte[] bytes = new byte[1024];
        List<String> imageUrls = null;
        LinesSearchInputStream inputStream = null;
        try {
            HttpStreamModel model = httpStreamReader.fromUri(uri);
            inputStream = new LinesSearchInputStream(model.stream, "\"fullimg\":[", ",]");
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

    private final Pattern arrayItemPattern = Pattern.compile("\"(.*?)\"");

    private List<String> extractUrls(final String str) {
        Log.d(TAG, "a: " + str);
        List<String> urls = new ArrayList<String>();
        final String newStr = str + ","; //savant
        Matcher matcher = arrayItemPattern.matcher(newStr);
        while (matcher.find()) {
            String url = matcher.group(1);
            urls.add(url);
        }
        return urls;
    }

    @Override
    public String getBaseSearchUri() {
        return baseSearchUri;
    }

    @Override
    public String getBaseUri() {
        return baseUri;
    }

    @NonNull
    @Override
    public List<FilterGroup> getFilters() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public List<Genre> getGenres() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public RequestPreprocessor getRequestPreprocessor() {
        return null;
    }

}