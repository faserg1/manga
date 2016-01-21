package com.danilov.supermanga.core.repository.special.test;

import com.danilov.supermanga.core.repository.special.JavaScriptEngine;

import java.io.Reader;
import java.io.StringReader;

/**
 * Created by Semyon on 21.01.2016.
 */
public class JSTestEngine extends JavaScriptEngine {

    public JSTestEngine(final String name, final String filePath) {
        super(name, filePath);
    }

    @Override
    public Reader getScript() {
        return new StringReader("function loge(a,b){helper.loge(a,b)}function logi(a,b){helper.logi(a,b)}function getSuggestions(a){loge(MTAG,\"loge Started\"),logi(MTAG,a),helper.loadPage(\"yandex.ru\"),loge(MTAG,a),logi(MTAG,\"logi Finished\");var b={title:\"First suggestion\",link:\"http://yandex.ru\"},c={title:\"Second suggestion\",link:\"http://google.com\"};return[b,c]}var MTAG=\"MyFirstJSEngine\";");
    }

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public boolean requiresAuth() {
        return false;
    }
}
