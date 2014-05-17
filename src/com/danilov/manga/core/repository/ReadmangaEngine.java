package com.danilov.manga.core.repository;

import com.danilov.manga.core.http.HttpBytesReader;
import com.danilov.manga.core.http.HttpRequestException;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.util.IoUtils;
import com.danilov.manga.core.util.ServiceContainer;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Semyon Danilov on 17.05.2014.
 */

/**
 * Engine for russian most popular manga site, geh
 */
public class ReadmangaEngine implements RepositoryEngine {

    private String baseSearchUri = "http://readmanga.me/search?q=";

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
        List<Manga> mangaList = new LinkedList<Manga>();
        if (httpBytesReader != null) {
            try {
                String uri = baseSearchUri + URLEncoder.encode(query, Charset.forName(HTTP.UTF_8).name());
                byte[] response = httpBytesReader.fromUri(uri);
                String responseString = IoUtils.convertBytesToString(response);
                responseString.isEmpty();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (HttpRequestException e) {
                e.printStackTrace();
            }
        }
        return mangaList;
    }

    @Override
    public String getBaseSearchUri() {
        return null;
    }

}
