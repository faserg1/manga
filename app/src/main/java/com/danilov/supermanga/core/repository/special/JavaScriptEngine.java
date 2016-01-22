package com.danilov.supermanga.core.repository.special;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danilov.supermanga.core.http.ExtendedHttpClient;
import com.danilov.supermanga.core.http.RequestPreprocessor;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.MangaSuggestion;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.util.IoUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon on 21.01.2016.
 *
 * Судя по всему придётся сделать отдельный интерфейс Repository
 * а текущий enum Repository нужно будет переименовать в DefaultRepository
 *
 */
public abstract class JavaScriptEngine implements RepositoryEngine {

    private String name;
    private String filePath;

    public JavaScriptEngine(final String name, final String filePath) {
        this.name = name;
        this.filePath = filePath;
    }

    public static class UsefulObject {

        private static final String JavaTag = "JavaScriptEngine::UsefulObject";

        /**
         * Get method, only URL must be provided
         * @param url URL of a page
         * @return
         */
        public String get(final String url) {
            HttpClient httpClient = new ExtendedHttpClient();
            try {
                HttpResponse response = httpClient.execute(new HttpGet(url));
                byte[] result = IoUtils.convertStreamToBytes(response.getEntity().getContent());
                String responseString = IoUtils.convertBytesToString(result);
                return responseString;
            } catch (IOException e) {
                loge(JavaTag, e.getMessage());
                return "-1";
            }
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
        ScriptableObject.putConstProperty(scope, "$", usefulJSObject);
        return new JSB(rhino, scope);
    }

    @Override
    public List<MangaSuggestion> getSuggestions(final String query) throws RepositoryException {
        List<MangaSuggestion> mangaSuggestions = Collections.emptyList();
        final JSB jsb = initJSContext();
        final Scriptable scope = jsb.scope;
        final Context context = jsb.context;
        Function getSuggestionsFn = (Function) scope.get("getSuggestions", scope);
        Object callResult = getSuggestionsFn.call(context, scope, scope, new Object[]{query});
        if (callResult == null) {
            return mangaSuggestions;
        }
        if (!(callResult instanceof NativeArray)) {
            throw new RepositoryException("Function getSuggestions must return an array of objects");
        }
        NativeArray result = (NativeArray) callResult;
        mangaSuggestions = new ArrayList<>(result.size());

        for (Object object : result) {
            if (!(object instanceof NativeObject)) {
                throw new RepositoryException("Every item of array must be JS an object");
            }
            NativeObject nativeObject = (NativeObject) object;
            Object mangaTitleJSString = nativeObject.get("mangaTitle");
            if (mangaTitleJSString == null) {
                throw new RepositoryException("Manga title must be provided in suggestion object");
            }
            String mangaTitle = mangaTitleJSString.toString();
            Object mangaUrlJSString = nativeObject.get("mangaUrl");
            if (mangaUrlJSString == null) {
                throw new RepositoryException("Manga URL must be provided in suggestion object");
            }
            String mangaUrl = mangaUrlJSString.toString();

            MangaSuggestion mangaSuggestion = new MangaSuggestion(mangaTitle, mangaUrl, Repository.MANGACHAN);
            mangaSuggestions.add(mangaSuggestion);
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

    @Nullable
    @Override
    public RequestPreprocessor getRequestPreprocessor() {
        return null;
    }
}
