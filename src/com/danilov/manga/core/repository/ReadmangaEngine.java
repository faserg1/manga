package com.danilov.manga.core.repository;

import com.danilov.manga.core.http.HttpBytesReader;
import com.danilov.manga.core.http.HttpRequestException;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.util.IoUtils;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.util.Utils;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
                mangaList = parseResponse(Utils.parseForDocument(responseString));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (HttpRequestException e) {
                e.printStackTrace();
            }
        }
        return mangaList;
    }

    //html values
    private String searchElementId = "mangaResults";
    private String mangaLinkClass = "manga-link";
    private String mangaCoverClass = "screenshot";
    private String mangaCoverLinkAttrName = "rel";

    //!html values

    private List<Manga> parseResponse(final Document document) {
        List<Manga> mangaList = new LinkedList<Manga>();
        Element searchResults = document.getElementById(searchElementId);
        List<Element> mangaLinks = searchResults.getElementsByClass(mangaLinkClass);
        for (Element mangaLink : mangaLinks) {
            Element parent = mangaLink.parent();
            String uri = baseUri + mangaLink.attr("href");
            String mangaName = String.valueOf(mangaLink.text());
            Element screenElement = parent.getElementsByClass(mangaCoverClass).get(0);
            String coverUri = screenElement != null ? screenElement.attr(mangaCoverLinkAttrName) : null;
            Manga manga = new Manga(mangaName, uri);
            manga.setCoverUri(coverUri);
            mangaList.add(manga);
        }
        return mangaList;
    }

    @Override
    public String getBaseSearchUri() {
        return null;
    }

}
