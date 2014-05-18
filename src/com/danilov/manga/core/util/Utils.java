package com.danilov.manga.core.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by Semyon Danilov on 18.05.2014.
 */
public class Utils {

    public static final Document parseForDocument(final String content) {
        Document doc = Jsoup.parse(content);
        return doc;
    }

}
