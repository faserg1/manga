package com.danilov.mangareaderplus.core.repository;

import android.util.Log;

import com.danilov.mangareaderplus.core.http.ExtendedHttpClient;
import com.danilov.mangareaderplus.core.http.HttpBytesReader;
import com.danilov.mangareaderplus.core.http.HttpRequestException;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.model.MangaSuggestion;
import com.danilov.mangareaderplus.core.util.IoUtils;
import com.danilov.mangareaderplus.core.util.ServiceContainer;
import com.danilov.mangareaderplus.core.util.Utils;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
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
 * Created by Semyon on 03.06.2015.
 */
public class KissmangaEngine implements RepositoryEngine {

    private final String baseSearchUri = "http://kissmanga.com/Search/Manga?keyword=";

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public List<MangaSuggestion> getSuggestions(final String query) throws RepositoryException {
        return Collections.emptyList();
    }

    @Override
    public List<Manga> queryRepository(final String query, final List<Filter.FilterValue> filterValues) {
        List<Manga> mangaList = null;
        try {
            String uri = baseSearchUri + URLEncoder.encode(query, Charset.forName(HTTP.UTF_8).name());
            for (Filter.FilterValue filterValue : filterValues) {
                uri = filterValue.apply(uri);
            }
            DefaultHttpClient httpClient = new ExtendedHttpClient();
            HttpPost request = new HttpPost(uri);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("keyword", query));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(request);
            byte[] result = IoUtils.convertStreamToBytes(response.getEntity().getContent());

            String responseString = IoUtils.convertBytesToString(result);

            mangaList = parseMangaSearchResponse(Utils.toDocument(responseString));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mangaList;
    }

    //html values
    private String linkValueAttr = "href";
    private String RESULTS_CLASS = "listing";
    private String RESULT_TAG = "td";

    private final Pattern urlPattern = Pattern.compile("src=\"(.*?)\"");

    private List<Manga> parseMangaSearchResponse(final Document document) {
        List<Manga> mangas = new ArrayList<>();
        Element results = document.getElementsByClass(RESULTS_CLASS).first();
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
                Matcher matcher = urlPattern.matcher(titleInner);
                if (matcher.find()) {
                    coverUri = matcher.group(1);
                }
                Manga manga = new Manga(title, url, Repository.KISSMANGA);
                manga.setCoverUri(coverUri);
                mangas.add(manga);
            }
            i++;
        }
        return mangas;
    }

    @Override
    public List<Manga> queryRepository(final Genre genre) {
        return Collections.emptyList();
    }

    @Override
    public boolean queryForMangaDescription(final Manga manga) throws RepositoryException {
        return false;
    }

    @Override
    public boolean queryForChapters(final Manga manga) throws RepositoryException {
        return false;
    }

    @Override
    public List<String> getChapterImages(final MangaChapter chapter) throws RepositoryException {
        return null;
    }

    @Override
    public String getBaseSearchUri() {
        return null;
    }

    @Override
    public String getBaseUri() {
        return null;
    }

    @Override
    public List<FilterGroup> getFilters() {
        return Collections.emptyList();
    }

    @Override
    public List<Genre> getGenres() {
        return Collections.emptyList();
    }

}