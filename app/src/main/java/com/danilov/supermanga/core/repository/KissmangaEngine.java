package com.danilov.supermanga.core.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danilov.supermanga.core.http.LinesSearchInputStream;
import com.danilov.supermanga.core.http.RequestPreprocessor;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.MangaSuggestion;
import com.danilov.supermanga.core.repository.special.CloudFlareBypassEngine;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.IoUtils;
import com.danilov.supermanga.core.util.Utils;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Semyon on 03.06.2015.
 */
public class KissmangaEngine extends CloudFlareBypassEngine {

    private static final String TAG = "KissmangaEngine";

    private final String baseSearchUri = "http://kissmanga.com/Search/Manga?keyword=";
    public static final String baseUri = "http://kissmanga.com";
    public static final String domain = "kissmanga.com";
    public static final String baseSuggestUri = "http://kissmanga.com/Search/SearchSuggest";

    @Override
    public String getLanguage() {
        return "English";
    }

    @Override
    public boolean requiresAuth() {
        return false;
    }

    private String suggestionPattern = "a href=\"(.*?)\">(.*?)<\\/a>";

    @Override
    public List<MangaSuggestion> getSuggestions(final String query) throws RepositoryException {
        List<MangaSuggestion> mangaSuggestions = new ArrayList<>();
        try {
            HttpPost request = new HttpPost(baseSuggestUri);
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("keyword", query));
            nameValuePairs.add(new BasicNameValuePair("type", "Manga"));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = loadPage(request);
            byte[] result = IoUtils.convertStreamToBytes(response.getEntity().getContent());
            String responseString = IoUtils.convertBytesToString(result);

            Pattern p = Pattern.compile(suggestionPattern);
            Matcher m = p.matcher(responseString);
            while (m.find()) {
                String link = m.group(1);
                int idx = link.indexOf(".com/Manga");
                if (idx != -1) {
                    link = link.substring(idx + 4);
                }
                String title = m.group(2);
                MangaSuggestion suggestion = new MangaSuggestion(title, link, DefaultRepository.KISSMANGA);
                mangaSuggestions.add(suggestion);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mangaSuggestions;
    }

    @Override
    public List<Manga> queryRepository(final String query, final List<Filter.FilterValue> filterValues) throws RepositoryException {
        List<Manga> mangaList = null;
        try {
            String uri = baseSearchUri + URLEncoder.encode(query, Charset.forName(HTTP.UTF_8).name());
            for (Filter.FilterValue filterValue : filterValues) {
                uri = filterValue.apply(uri);
            }
            HttpPost request = new HttpPost(uri);

            List<NameValuePair> nameValuePairs = new ArrayList<>(1);
            nameValuePairs.add(new BasicNameValuePair("keyword", query));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpContext context = new BasicHttpContext();
            HttpResponse response = loadPage(request, context);

            HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(
                    ExecutionContext.HTTP_REQUEST);
            HttpHost currentHost = (HttpHost)  context.getAttribute(
                    ExecutionContext.HTTP_TARGET_HOST);
            String currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq.getURI().toString() : (currentHost.toURI() + currentReq.getURI());


            byte[] result = IoUtils.convertStreamToBytes(response.getEntity().getContent());
            String responseString = IoUtils.convertBytesToString(result);

            if (currentUrl.contains("kissmanga.com/Manga")) {
                Manga manga = new Manga("", currentUrl.replace(baseUri, ""), DefaultRepository.KISSMANGA);
                parseMangaDescriptionResponse(manga, Utils.toDocument(responseString));
                if (!"".equals(manga.getTitle())) {
                    mangaList = new ArrayList<>(1);
                    mangaList.add(manga);
                }
            } else if (currentUrl.contains("kissmanga.com/Search")) {
                mangaList = parseMangaSearchResponse(Utils.toDocument(responseString));
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to load: " + e.getMessage());
        }
        return mangaList;
    }

    //html values
    private String linkValueAttr = "href";
    private String RESULTS_CLASS = "listing";
    private String RESULT_TAG = "td";

    private final Pattern srcPattern = Pattern.compile("src=\"(.*?)\"");

    private List<Manga> parseMangaSearchResponse(final Document document) {
        List<Manga> mangas = new ArrayList<>();
        Element results = document.getElementsByClass(RESULTS_CLASS).first();
        if (results == null) {
            return mangas;
        }
        Elements mangaResults = results.getElementsByTag(RESULT_TAG);
        int i = 0;
        for (Element td : mangaResults) {
            if (i % 2 == 1) {
                i++;
                continue;
            }
            Element link = td.getElementsByTag("a").first();
            if (link != null) {
                String title = link.text();
                String url = link.attr(linkValueAttr);
                String coverUri = null;

                String titleInner = td.attr("title");
                Matcher matcher = srcPattern.matcher(titleInner);
                if (matcher.find()) {
                    coverUri = matcher.group(1);
                }
                Manga manga = new Manga(title, url, DefaultRepository.KISSMANGA);
                manga.setCoverUri(coverUri);
                mangas.add(manga);
            }
            i++;
        }
        return mangas;
    }

    @Override
    public List<Manga> queryRepository(final Genre genre) throws RepositoryException {
        return Collections.emptyList();
    }

    @Override
    public boolean queryForMangaDescription(final Manga manga) throws RepositoryException {
        String uri = baseUri + manga.getUri();
        try {
            HttpGet request = new HttpGet(uri);
            HttpResponse response = loadPage(request);
            byte[] result = IoUtils.convertStreamToBytes(response.getEntity().getContent());
            String responseString = IoUtils.convertBytesToString(result);
            return parseMangaDescriptionResponse(manga, Utils.toDocument(responseString));
        } catch (IOException e) {
            if (e.getMessage() != null) {
                Log.d(TAG, e.getMessage());
            } else {
                Log.d(TAG, "Failed to load manga description");
            }
            throw new RepositoryException(e.getMessage());
        }
    }

    private boolean parseMangaDescriptionResponse(final Manga manga, final Document document) {
        try {
            Element titleElement = document.getElementById("leftside").getElementsByClass("barContent").first().getElementsByClass("bigChar").first();
            Element parent = titleElement.parent();
            Element description = parent.child(6);

            Element authors = parent.child(3);
            StringBuilder lst = new StringBuilder();
            Elements listElement = authors.getElementsByTag("a");
            if (!listElement.isEmpty()) {
                int i = 0;
                for (Element links : listElement) {
                    String txt = links.text();
                    if (i > 0) {
                        lst.append(", ");
                    }
                    lst.append(txt);
                    i++;
                }
            }
            manga.setAuthor(lst.toString());


            Element genres = parent.child(2);
            lst = new StringBuilder();
            listElement = genres.getElementsByTag("a");
            if (!listElement.isEmpty()) {
                int i = 0;
                for (Element links : listElement) {
                    String txt = links.text();
                    if (i > 0) {
                        lst.append(", ");
                    }
                    lst.append(txt);
                    i++;
                }
            }
            manga.setGenres(lst.toString());

            manga.setDescription(description.text());
            manga.setTitle(titleElement.text());
            if (manga.getCoverUri() == null || manga.getCoverUri().length() <= 0) {
                Element img = document.getElementById("rightside").getElementsByTag("img").first();
                manga.setCoverUri(img.attr("src"));
            }
            Element results = document.getElementsByClass(RESULTS_CLASS).first();
            int chaptersQuantity = results.getElementsByTag("td").size() / 2;
            manga.setChaptersQuantity(chaptersQuantity);
            return true;
        } catch (Exception e) {

        }

        return false;
    }

    @Override
    public boolean queryForChapters(final Manga manga) throws RepositoryException {
        String uri = baseUri + manga.getUri();
        try {
            HttpGet request = new HttpGet(uri);
            HttpResponse response = loadPage(request);
            byte[] result = IoUtils.convertStreamToBytes(response.getEntity().getContent());
            String responseString = IoUtils.convertBytesToString(result);
            List<MangaChapter> chapters = parseMangaChaptersResponse(Utils.toDocument(responseString));
            if (chapters != null) {
                manga.setChapters(chapters);
                return true;
            }
        } catch (IOException e) {
            if (e.getMessage() != null) {
                Log.d(TAG, e.getMessage());
            } else {
                Log.d(TAG, "Failed to load manga chapters");
            }
            throw new RepositoryException(e.getMessage());
        }
        return false;
    }

    private List<MangaChapter> parseMangaChaptersResponse(final Document document) {
        List<MangaChapter> mangaChapters = null;
        try {
            Element results = document.getElementsByClass(RESULTS_CLASS).first();
            Elements as = results.getElementsByTag("a");
            int chaptersQuantity = as.size();
            mangaChapters = new ArrayList<>(chaptersQuantity);
            int number = 0;
            for (int i = as.size() - 1; i >= 0; i--) {
                Element link = as.get(i);
                MangaChapter chapter = new MangaChapter(link.text(), number, link.attr(linkValueAttr));
                mangaChapters.add(chapter);
                number++;
            }
            return mangaChapters;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getChapterImages(final MangaChapter chapter) throws RepositoryException {
        String uri = baseUri + chapter.getUri();
        byte[] bytes = new byte[1024];
        List<String> imageUrls = null;
        LinesSearchInputStream inputStream = null;
        try {

            HttpGet request = new HttpGet(uri);
            HttpResponse response = loadPage(request);
            byte[] result = IoUtils.convertStreamToBytes(response.getEntity().getContent());
            String responseString = IoUtils.convertBytesToString(result);
            InputStream stringStream = new ByteArrayInputStream(responseString.getBytes());

            inputStream = new LinesSearchInputStream(stringStream, "var lstImages = new Array();", "var lstImagesLoaded = new Array();");
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

    private final Pattern urlPattern = Pattern.compile("\"(.*?)\"");

    private List<String> extractUrls(final String str) {
        Log.d(TAG, "a: " + str);
        Matcher matcher = urlPattern.matcher(str);
        List<String> urls = new ArrayList<>();
        while (matcher.find()) {
            String url = matcher.group(1);
            if (!url.contains("http")) {
                url = baseUri + url;
            }
            urls.add(url);
        }
        return urls;
    }

    @Override
    public String getBaseSearchUri() {
        return null;
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
        return preprocessor;
    }

    @Override
    @NonNull
    public String getDomain() {
        return domain;
    }

    @NonNull
    @Override
    public String getEmptyRequestURL() {
        return baseUri;
    }

    private RequestPreprocessor preprocessor = new RequestPreprocessor() {

        private final Lock cookieLock = new ReentrantLock();
        private final Lock cookieHandlerLock = new ReentrantLock();
        private volatile CookieStore cookieStore;

        private boolean cookieHandlerSet = false;

        @Override
        public HttpURLConnection process(@NonNull final URL url) throws IOException {
            CookieStore cookieStore = loadCookies();
            if (cookieStore != null) {
                if (!cookieHandlerSet) {
                    try {
                        cookieHandlerLock.lock();
                        setCookieHandler(cookieStore);
                        cookieHandlerSet = true;
                    } finally {
                        cookieHandlerLock.unlock();
                    }
                }
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT_STRING);
            return connection;
        }

        @Override
        public void process(@NonNull final DefaultHttpClient httpClient) {
            CookieStore cookieStore = loadCookies();
            if (cookieStore != null) {
                httpClient.setCookieStore(cookieStore);
            }
        }

        private CookieStore loadCookies() {
            if (cookieStore == null) {
                try {
                    cookieLock.lock();
                    cookieStore = getCookieStore();
                    if (cookieStore == null) {
                        try {
                            emptyRequest();
                            cookieStore = getCookieStore();
                        } catch (IOException | RepositoryException e) {
                            //ну а что мы можем сделать, если куки нет
                        }
                    }
                } finally {
                    cookieLock.unlock();
                }
            }
            return cookieStore;
        }

        private void setCookieHandler(final CookieStore cookieStore) {
            List<Cookie> cookies = cookieStore.getCookies();

            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);

            java.net.CookieStore hucCS = cookieManager.getCookieStore();

            for (Cookie cookie : cookies) {
                HttpCookie httpCookie = new HttpCookie(cookie.getName(), cookie.getValue());
                httpCookie.setDomain(cookie.getDomain());
                httpCookie.setPath("/");
                httpCookie.setVersion(0);

                hucCS.add(URI.create(cookie.getDomain()), httpCookie);
            }
        }

    };

}