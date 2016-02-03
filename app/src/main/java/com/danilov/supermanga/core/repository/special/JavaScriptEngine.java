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
import com.danilov.supermanga.core.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon on 21.01.2016.
 *
 */
public abstract class JavaScriptEngine implements RepositoryEngine {

    private String name;
    private String filePath;
    private Repository repository;

    public JavaScriptEngine(final String name, final String filePath) {
        this.name = name;
        this.filePath = filePath;
    }

    public void setRepository(final Repository repository) {
        this.repository = repository;
    }

    public Repository getRepository() {
        return repository;
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
                return "null";
            }
        }

        public DocHelper parseDoc(final String content) {
            return new DocHelper(content);
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
                throw new RepositoryException("Every item of array must be an object");
            }
            NativeObject nativeObject = (NativeObject) object;
            String mangaTitle = getString(nativeObject, "mangaTitle");
            String mangaUrl = getString(nativeObject, "mangaUrl");
            MangaSuggestion mangaSuggestion = new MangaSuggestion(mangaTitle, mangaUrl, repository);
            mangaSuggestions.add(mangaSuggestion);
        }

        return mangaSuggestions;
    }

    @Override
    public List<Manga> queryRepository(final String query, final List<Filter.FilterValue> filterValues) throws RepositoryException {
        List<Manga> mangaList = Collections.emptyList();
        final JSB jsb = initJSContext();
        final Scriptable scope = jsb.scope;
        final Context context = jsb.context;
        Function queryRepositoryFn = (Function) scope.get("queryRepository", scope);
        Object callResult = queryRepositoryFn.call(context, scope, scope, new Object[]{query});
        if (callResult == null) {
            return mangaList;
        }
        if (!(callResult instanceof NativeArray)) {
            throw new RepositoryException("Function queryRepository must return an array of objects");
        }
        NativeArray result = (NativeArray) callResult;
        mangaList = new ArrayList<>(result.size());

        for (Object object : result) {
            if (!(object instanceof NativeObject)) {
                throw new RepositoryException("Every item of array must be an object");
            }
            NativeObject nativeObject = (NativeObject) object;
            String mangaTitle = getString(nativeObject, "mangaTitle");
            String mangaUrl = getString(nativeObject, "mangaUrl");
            String mangaCoverUrl = getString(nativeObject, "mangaCover");
            Manga manga = new Manga(mangaTitle, mangaUrl, repository);
            manga.setCoverUri(mangaCoverUrl);
            mangaList.add(manga);
        }

        return mangaList;
    }

    @Override
    public List<Manga> queryRepository(final Genre genre) throws RepositoryException {
        return null;
    }

    @Override
    public boolean queryForMangaDescription(final Manga manga) throws RepositoryException {
        final JSB jsb = initJSContext();
        final Scriptable scope = jsb.scope;
        final Context context = jsb.context;
        Function queryRepositoryFn = (Function) scope.get("queryForMangaDescription", scope);
        Object callResult = queryRepositoryFn.call(context, scope, scope, new Object[]{manga.getUri()});
        if (callResult == null) {
            return false;
        }
        if (!(callResult instanceof NativeObject)) {
            throw new RepositoryException("Function queryForMangaDescription must return an object");
        }
        NativeObject nativeObject = (NativeObject) callResult;

        String mangaDescription = getString(nativeObject, "mangaDescription");
        String mangaAuthor = getString(nativeObject, "mangaAuthor");
        String mangaGenres = getString(nativeObject, "mangaGenres");
        Integer chaptersQuantity = getInteger(nativeObject, "chaptersQuantity");

        manga.setDescription(mangaDescription);
        manga.setGenres(mangaGenres);
        manga.setAuthor(mangaAuthor);
        manga.setChaptersQuantity(chaptersQuantity);

        return true;
    }

    @Override
    public boolean queryForChapters(final Manga manga) throws RepositoryException {
        final JSB jsb = initJSContext();
        final Scriptable scope = jsb.scope;
        final Context context = jsb.context;
        Function queryRepositoryFn = (Function) scope.get("queryForChapters", scope);
        Object callResult = queryRepositoryFn.call(context, scope, scope, new Object[]{manga.getUri()});
        if (callResult == null) {
            return false;
        }

        if (!(callResult instanceof NativeArray)) {
            throw new RepositoryException("Function queryRepository must return an array of objects");
        }
        NativeArray result = (NativeArray) callResult;
        List<MangaChapter> mangaChapters = new ArrayList<>(result.size());
        int i = 0;
        for (Object object : result) {
            if (!(object instanceof NativeObject)) {
                throw new RepositoryException("Every item of array must be an object");
            }
            NativeObject nativeObject = (NativeObject) object;
            String chapterTitle = getString(nativeObject, "chapterTitle");
            String chapterUrl = getString(nativeObject, "chapterUrl");

            MangaChapter mangaChapter = new MangaChapter(chapterTitle, i, chapterUrl);
            mangaChapters.add(mangaChapter);
            i++;
        }
        manga.setChaptersQuantity(mangaChapters.size());
        manga.setChapters(mangaChapters);
        return true;
    }

    @Override
    public List<String> getChapterImages(final MangaChapter chapter) throws RepositoryException {
        final JSB jsb = initJSContext();
        final Scriptable scope = jsb.scope;
        final Context context = jsb.context;
        Function queryRepositoryFn = (Function) scope.get("getChapterImages", scope);
        Object callResult = queryRepositoryFn.call(context, scope, scope, new Object[]{chapter.getUri()});
        if (callResult == null) {
            return null;
        }

        if (!(callResult instanceof NativeArray)) {
            throw new RepositoryException("Function getChapterImages must return an array of strings");
        }
        NativeArray result = (NativeArray) callResult;
        List<String> chapterImages = new ArrayList<>(result.size());
        for (Object object : result) {
            if (!(object instanceof CharSequence)) {
                throw new RepositoryException("Every item of array must be a string");
            }
            chapterImages.add(object.toString());
        }
        return chapterImages;
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

    private static String getString(final NativeObject object, final String name) throws RepositoryException {
        Object obj = object.get(name);
        if (obj == null || (!(obj instanceof CharSequence))) {
            throw new RepositoryException("Object must contain string value '" + name + "'");
        }
        return obj.toString();
    }

    private static Integer getInteger(final NativeObject object, final String name) throws RepositoryException {
        Object obj = object.get(name);
        if (obj == null) {
            throw new RepositoryException("Object must contain number value '" + name + "'");
        }

        if (obj instanceof CharSequence) {
            String num = obj.toString();
            try {
                return Integer.parseInt(num);
            } catch (NumberFormatException e) {
                throw new RepositoryException("Object must contain number value '" + name + "': " + e.getMessage());
            }
        }

        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        throw new RepositoryException("Object must contain number value '" + name + "'");
    }


    public static class DocHelper {

        private Document document;

        public DocHelper(final Document document) {
            this.document = document;
        }

        public DocHelper(final String content) {
            this.document = Utils.toDocument(content);

        }

        public ElementsHelper select(final String cssQuery) {
            return new ElementsHelper(this.document.select(cssQuery));
        }

    }

    public static class ElementsHelper {

        private Elements elements;

        public ElementsHelper(final Elements elements) {
            this.elements = elements;
        }

        public ElementsHelper select(final String cssQuery) {
            return new ElementsHelper(this.elements.select(cssQuery));
        }

        public ElementsHelper get(final int i) {
            return new ElementsHelper(wrapElement(this.elements.get(i)));
        }

        public String text() {
            return this.elements.text();
        }

        public String attr(final String attr) {
            return this.elements.attr(attr);
        }

        public int size() {
            return this.elements.size();
        }

        public int childrenSize() {
            if (this.elements.size() == 1) {
                return elements.get(0).childNodeSize();
            }
            return 0;
        }

        public ElementsHelper parent() {
            if (this.elements.size() == 1) {
                return new ElementsHelper(wrapElement(elements.get(0).parent()));
            }
            return null;
        }

        public ElementsHelper getChild(final int i) {
            if (this.elements.size() == 1) {
                return new ElementsHelper(wrapElement(elements.get(0).child(i)));
            }
            return null;
        }

        private static Elements wrapElement(final Element element) {
            ArrayList<Element> singleElementList = new ArrayList<>(1);
            singleElementList.add(element);
            return new Elements(singleElementList);
        }

    }

}
