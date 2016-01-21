package com.danilov.supermanga.core.repository.special;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danilov.supermanga.core.http.RequestPreprocessor;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.MangaSuggestion;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon on 21.01.2016.
 */
public abstract class JavaScriptEngine implements RepositoryEngine {

    private String name;
    private String filePath;

    public JavaScriptEngine(final String name, final String filePath) {
        this.name = name;
        this.filePath = filePath;
    }

    public static class UsefulObject {

        public String loadPage(final String page) {
            return "hthmthltht";
        }

        public void loge(final String tag, final String error) {
            Log.e(tag, error);
        }
        public void logi(final String tag, final String info) {
            Log.i(tag, info);
        }

    }

    private class JSB {

        Context context;

        Scriptable scope;

        public JSB(final Context context, final Scriptable scope) {
            this.context = context;
            this.scope = scope;
        }
    }

    public Reader getScript() {
        try {
            return new FileReader(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSB initJSContext() {
        // инициализируем Rhino
        Context rhino = Context.enter();
        rhino.setOptimizationLevel(-1);
        Scriptable scope = rhino.initStandardObjects(); // инициализируем пространство исполнения

//        try {
//            rhino.evaluateReader(scope, new FileReader(filePath), name, 1, null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            rhino.evaluateReader(scope, getScript(), name, 1, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Object usefulJSObject = Context.javaToJS(new UsefulObject(), scope);
        ScriptableObject.putConstProperty(scope, "helper", usefulJSObject);
        return new JSB(rhino, scope);
    }

    @Override
    public List<MangaSuggestion> getSuggestions(final String query) throws RepositoryException {
        final JSB jsb = initJSContext();
        final Scriptable scope = jsb.scope;
        final Context context = jsb.context;
        Function getSuggestionsFn = (Function) scope.get("getSuggestions", scope);
        Object callResult = getSuggestionsFn.call(context, scope, scope, new Object[]{query});
        if (callResult != null) {

        }
        return Collections.emptyList();
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
        return null;
    }

    @NonNull
    @Override
    public List<Genre> getGenres() {
        return null;
    }

    @Nullable
    @Override
    public RequestPreprocessor getRequestPreprocessor() {
        return null;
    }
}
