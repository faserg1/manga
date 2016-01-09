package com.danilov.supermanga.core.repository;

import android.support.annotation.NonNull;

import com.danilov.supermanga.core.http.ExtendedHttpClient;
import com.danilov.supermanga.core.http.HttpBytesReader;
import com.danilov.supermanga.core.http.HttpRequestException;
import com.danilov.supermanga.core.http.RequestPreprocessor;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.MangaSuggestion;
import com.danilov.supermanga.core.repository.special.AuthorizableEngine;
import com.danilov.supermanga.core.util.IoUtils;
import com.danilov.supermanga.core.util.ServiceContainer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

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

    private String baseSuggestionUri = "http://mangachan.ru/engine/ajax/search.php";

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
                MangaSuggestion suggestion = new MangaSuggestion(title, link, Repository.MANGACHAN);
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
        return null;
    }

    @Override
    public List<Manga> queryRepository(final Genre genre) throws RepositoryException {
        return null;
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

    @Override
    public RequestPreprocessor getRequestPreprocessor() {
        return null;
    }

}