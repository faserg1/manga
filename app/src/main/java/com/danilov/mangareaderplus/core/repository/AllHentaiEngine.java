package com.danilov.mangareaderplus.core.repository;

import android.util.Log;

import com.danilov.mangareaderplus.core.http.HttpBytesReader;
import com.danilov.mangareaderplus.core.http.HttpRequestException;
import com.danilov.mangareaderplus.core.http.HttpStreamModel;
import com.danilov.mangareaderplus.core.http.HttpStreamReader;
import com.danilov.mangareaderplus.core.http.LinesSearchInputStream;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.model.MangaSuggestion;
import com.danilov.mangareaderplus.core.repository.filter.BasicFilters;
import com.danilov.mangareaderplus.core.util.IoUtils;
import com.danilov.mangareaderplus.core.util.ServiceContainer;
import com.danilov.mangareaderplus.core.util.Utils;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Semyon on 14.03.2015.
 */
public class AllHentaiEngine implements RepositoryEngine {

    private static final String TAG = "AllHentaiEngine";

    private String baseSearchUri = "http://allhentai.ru/search?q=";
    private String baseSuggestionUri = "http://allhentai.ru/search/suggestion?term=";
    public static final String baseUri = "http://allhentai.ru";

    @Override
    public String getLanguage() {
        return "Русский";
    }

    @Override
    public List<MangaSuggestion> getSuggestions(final String query) throws RepositoryException {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        List<MangaSuggestion> suggestions = null;
        if (httpBytesReader != null) {
            Exception ex = null;
            try {
                String uri = baseSuggestionUri + URLEncoder.encode(query, Charset.forName(HTTP.UTF_8).name());
                byte[] response = httpBytesReader.fromUri(uri);
                String responseString = IoUtils.convertBytesToString(response);
                suggestions = parseSuggestionsResponse(Utils.toJSONArray(responseString));
            } catch (UnsupportedEncodingException e) {
                ex = e;
            } catch (HttpRequestException e) {
                ex = e;
            } catch (JSONException e) {
                ex = e;
            }
            if (ex != null) {
                throw new RepositoryException(ex.getMessage());
            }
        }
        return suggestions;
    }

    @Override
    public List<Manga> queryRepository(final String query, final List<Filter.FilterValue> filterValues) {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        List<Manga> mangaList = null;
        if (httpBytesReader != null) {
            try {
                String uri = baseSearchUri + URLEncoder.encode(query, Charset.forName(HTTP.UTF_8).name());
                for (Filter.FilterValue filterValue : filterValues) {
                    uri = filterValue.apply(uri);
                }
                byte[] response = httpBytesReader.fromUri(uri);
                String responseString = IoUtils.convertBytesToString(response);
                mangaList = parseSearchResponse(Utils.toDocument(responseString));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (HttpRequestException e) {
                e.printStackTrace();
            }
        }
        return mangaList;
    }

    @Override
    public List<Manga> queryRepository(final Genre _genre) {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        List<Manga> mangaList = null;
        AllHentaiGenre genre = (AllHentaiGenre) _genre;
        if (httpBytesReader != null) {
            try {
                String uri = baseUri + genre.getLink();
                byte[] response = httpBytesReader.fromUri(uri);
                String responseString = IoUtils.convertBytesToString(response);
                mangaList = parseGenreSearchResponse(Utils.toDocument(responseString));
            } catch (HttpRequestException e) {
                e.printStackTrace();
            }
        }
        return mangaList;
    }

    @Override
    public boolean queryForMangaDescription(final Manga manga) throws RepositoryException {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        if (httpBytesReader != null) {
            String uri = baseUri + manga.getUri();
            byte[] response = null;
            try {
                response = httpBytesReader.fromUri(uri);
            } catch (HttpRequestException e) {
                if (e.getMessage() != null) {
                    Log.d(TAG, e.getMessage());
                } else {
                    Log.d(TAG, "Failed to load manga description");
                }
                throw new RepositoryException(e.getMessage());
            }
            String responseString = IoUtils.convertBytesToString(response);
            return parseMangaDescriptionResponse(manga, Utils.toDocument(responseString));
        }
        return false;
    }

    @Override
    public boolean queryForChapters(final Manga manga) throws RepositoryException {
        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        if (httpBytesReader != null) {
            String uri = baseUri + manga.getUri();
            byte[] response = null;
            try {
                response = httpBytesReader.fromUri(uri);
            } catch (HttpRequestException e) {
                Log.d(TAG, e.getMessage());
                throw new RepositoryException(e.getMessage());
            }
            String responseString = IoUtils.convertBytesToString(response);
            List<MangaChapter> chapters = parseMangaChaptersResponse(Utils.toDocument(responseString));
            if (chapters != null) {
                manga.setChapters(chapters);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getChapterImages(final MangaChapter chapter) throws RepositoryException {
        String uri = baseUri + chapter.getUri() + "?mature=1";
        HttpStreamReader httpStreamReader = ServiceContainer.getService(HttpStreamReader.class);
        byte[] bytes = new byte[1024];
        List<String> imageUrls = null;
        LinesSearchInputStream inputStream = null;
        try {
            HttpStreamModel model = httpStreamReader.fromUri(uri);
            inputStream = new LinesSearchInputStream(model.stream, "pictures = [", "];");
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
        } catch (HttpRequestException e) {
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

    private final Pattern urlPattern = Pattern.compile("url:\"(.*?)\"");

    private List<String> extractUrls(final String str) {
        Log.d(TAG, "a: " + str);
        Matcher matcher = urlPattern.matcher(str);
        List<String> urls = new ArrayList<String>();
        while (matcher.find()) {
            String url = matcher.group(1);
            if (!url.contains("http")) {
                url = baseUri + url;
            }
            urls.add(url);
        }
        return urls;
    }

    //json names

    private String suggestionsElementName = "suggestions";
    private String exclusionValue = "Автор";
    private String exclusionValue2 = "Переводчик";
    private String valueElementName = "label";
    private String linkElementName = "link";


    //!json names

    private List<MangaSuggestion> parseSuggestionsResponse(final JSONArray suggestions) {
        List<MangaSuggestion> mangaSuggestions = new ArrayList<MangaSuggestion>();
        try {
            for (int i = 0; i < suggestions.length(); i++) {
                JSONObject suggestion = suggestions.getJSONObject(i);
                String value = suggestion.getString(valueElementName);
                if (value == null
                        || value.contains(exclusionValue)
                        || value.contains(exclusionValue2)) {
                    continue;
                }
                try {
                    String link = suggestion.getString(linkElementName);
                    MangaSuggestion mangaSuggestion = new MangaSuggestion(value, link, Repository.ALLHENTAI);
                    mangaSuggestions.add(mangaSuggestion);
                } catch (JSONException e ) {
                    Log.d(TAG, e.getMessage());
                    continue;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
        return mangaSuggestions;
    }

    //html values
    private String searchElementId = "mangaResults";
    private String mangaLinkClass = "manga-link";
    private String mangaCoverClass = "screenshot";
    private String mangaCoverLinkAttrName = "rel";

    //!html values

    private List<Manga> parseSearchResponse(final Document document) {
        List<Manga> mangaList = null;
        Element searchResults = document.getElementById(searchElementId);
        List<Element> mangaLinks = searchResults.getElementsByTag("a");
        mangaList = new ArrayList<Manga>(mangaLinks.size());
        for (Element mangaLink : mangaLinks) {
            if (mangaLink.hasClass(mangaCoverClass)) {
                continue;
            }
            Element parent = mangaLink.parent();
            String uri = mangaLink.attr("href");
            mangaLink.getElementsByTag("sup").remove();
            String mangaName = String.valueOf(mangaLink.text());
            Manga manga = new Manga(mangaName, uri, Repository.ALLHENTAI);
            if (parent.children().size() > 1) {
                Element screenElement = parent.getElementsByClass(mangaCoverClass).get(0);
                String coverUri = screenElement != null ? screenElement.attr(mangaCoverLinkAttrName) : null;
                manga.setCoverUri(coverUri);
            }
            mangaList.add(manga);
        }
        return mangaList;
    }

    private List<Manga> parseGenreSearchResponse(final Document document) {
        List<Manga> mangaList = null;
        Element searchResults = document.getElementsByClass("cTable").last();
        List<Element> mangaLinks = searchResults.getElementsByTag("a");
        mangaList = new ArrayList<Manga>(mangaLinks.size());
        for (Element mangaLink : mangaLinks) {
            if (mangaLink.hasClass(mangaCoverClass)) {
                continue;
            }
            Element parent = mangaLink.parent();
            String uri = mangaLink.attr("href");
            mangaLink.getElementsByTag("sup").remove();
            String mangaName = String.valueOf(mangaLink.text());
            Manga manga = new Manga(mangaName, uri, Repository.ALLHENTAI);
            if (parent.children().size() > 1) {
                Element screenElement = parent.getElementsByClass(mangaCoverClass).first();
                String coverUri = screenElement != null ? screenElement.attr(mangaCoverLinkAttrName) : null;
                manga.setCoverUri(coverUri);
            }
            mangaList.add(manga);
        }
        return mangaList;
    }

    //html values
    private String genresClass = "tiles";

    //!html values

//    private List<Manga> parseGenreSearchResponse(final Document document) {
//        List<Manga> mangaList = null;
//        Elements els = document.getElementsByClass(genresClass);
//        if (els.isEmpty()) {
//            return null;
//        }
//        Element searchResults = els.first();
//        List<Element> mangaLinks = searchResults.getElementsByClass(mangaTileClass);
//        mangaList = new ArrayList<Manga>(mangaLinks.size());
//        for (Element mangaLink : mangaLinks) {
//            String uri = null;
//            String mangaName = null;
//            Elements tmp = mangaLink.getElementsByClass(mangaDescClass);
//            if (!tmp.isEmpty()) {
//                tmp = tmp.get(0).getElementsByTag("h3");
//                if (!tmp.isEmpty()) {
//                    tmp = tmp.get(0).getElementsByTag("a");
//                    if (!tmp.isEmpty()) {
//                        Element realLink = tmp.get(0);
//                        uri = realLink.attr("href");
//                        mangaName = realLink.attr("title");
//                    }
//                }
//            }
//
//            Element screenElement = mangaLink.getElementsByClass(mangaCoverClass).get(0);
//
//            tmp = screenElement.getElementsByTag("img");
//            String coverUri = null;
//            if (!tmp.isEmpty()) {
//                Element img = tmp.get(0);
//                coverUri = img != null ? img.attr("src") : "";
//            }
//            Manga manga = new Manga(mangaName, uri, Repository.READMANGA);
//            manga.setCoverUri(coverUri);
//            mangaList.add(manga);
//        }
//        return mangaList;
//    }

    //html values
    private String altCoverClass = "nivo-imageLink";

    private boolean parseMangaDescriptionResponse(final Manga manga, final Document document) {
        Elements mangaDescriptionElements = document.getElementsByAttributeValue("itemprop", "description");
        Elements links = null;
        if (!mangaDescriptionElements.isEmpty()) {
            Element mangaDescription = mangaDescriptionElements.first();
            links = mangaDescription.getElementsByTag("a");
            if (!links.isEmpty()) {
                links.remove();
            }
            String description = mangaDescription.text();
            manga.setDescription(description);
        }
        Elements chaptersElements = document.select(".expandable .cTable");
        int quantity = 0;
        if (chaptersElements.isEmpty()) {
            quantity = 0;
        } else {
            Element chaptersElement = chaptersElements.first();
            links = chaptersElement.getElementsByTag("a");
            quantity = links.size();
        }
        manga.setChaptersQuantity(quantity);
        if (manga.getCoverUri() != null) {
            return true;
        }
        Elements coverContainer = document.getElementsByClass("mangaDescPicture");
        String coverUri = null;
        if (coverContainer.size() >= 1) {
            Element cover = coverContainer.get(0);
            Elements coverUriElements = cover.getElementsByTag("img");
            if (coverUriElements.size() >= 1) {
                coverUriElements = cover.getElementsByClass(altCoverClass);
                if (coverUriElements.size() >= 1) {
                    //more than one
                    coverUri = coverUriElements.get(0).attr("href");
                } else {
                    //only one
                    Elements elements = cover.getElementsByTag("img");
                    if (elements.size() >= 1) {
                        coverUri = elements.get(0).attr("src");
                    }
                }
            } else {
                //a lot of images
                Element e = coverUriElements.get(0);
                coverUri = e.attr("src");
            }
        }
        manga.setCoverUri(coverUri);
        return true;
    }

    //html values
    private String linkValueAttr = "href";

    private List<MangaChapter> parseMangaChaptersResponse(final Document document) {
        Elements chaptersElements = document.select(".expandable .cTable");
        if (chaptersElements.isEmpty()) {
            return null;
        }
        Element chaptersElement = chaptersElements.first();
        Elements links = chaptersElement.getElementsByTag("a");
        if (links.isEmpty()) {
            return null;
        }
        List<MangaChapter> chapters = new ArrayList<MangaChapter>(links.size());
        int number = 0;
        for (int i = links.size() - 1; i >= 0; i--) {
            Element element = links.get(i);
            String link = element.attr(linkValueAttr);
            String title = element.text();
            if (title.contains("vip")) {
                continue;
            }
            MangaChapter chapter = new MangaChapter(title, number, link);
            chapters.add(chapter);
            number++;
        }
        return chapters;
    }

    @Override
    public String getBaseSearchUri() {
        return baseSearchUri;
    }

    @Override
    public String getBaseUri() {
        return baseUri;
    }

    private List<FilterGroup> filterGroups = new ArrayList<>(2);

    {
        FilterGroup categories = new FilterGroup("Категории", 4);
        categories.add(new BasicFilters.RABaseTriState("Сингл", "s_single"));
        categories.add(new BasicFilters.RABaseTriState("Для взрослых", "s_mature"));
        categories.add(new BasicFilters.RABaseTriState("Завершенная", "s_completed"));
        categories.add(new BasicFilters.RABaseTriState("Ожидает загрузки", "s_wait_upload"));
        FilterGroup genres = new FilterGroup("Жанр", 86);

        genres.add(new BasicFilters.RABaseTriState("3D", "el_626"));
        genres.add(new BasicFilters.RABaseTriState("Ahegao", "el_855"));
        genres.add(new BasicFilters.RABaseTriState("Footfuck", "el_912"));
        genres.add(new BasicFilters.RABaseTriState("Gender bender", "el_89"));
        genres.add(new BasicFilters.RABaseTriState("Mind break", "el_705"));
        genres.add(new BasicFilters.RABaseTriState("Tomboy", "el_881"));
        genres.add(new BasicFilters.RABaseTriState("Анал", "el_828"));
        genres.add(new BasicFilters.RABaseTriState("Бакуню", "el_79"));
        genres.add(new BasicFilters.RABaseTriState("Бдсм", "el_78"));
        genres.add(new BasicFilters.RABaseTriState("Без цензуры", "el_888"));
        genres.add(new BasicFilters.RABaseTriState("Больница", "el_289"));
        genres.add(new BasicFilters.RABaseTriState("Большая грудь", "el_837"));
        genres.add(new BasicFilters.RABaseTriState("Буккакэ", "el_82"));
        genres.add(new BasicFilters.RABaseTriState("В ванной", "el_878"));
        genres.add(new BasicFilters.RABaseTriState("В общественном месте", "el_866"));
        genres.add(new BasicFilters.RABaseTriState("В первый раз", "el_811"));
        genres.add(new BasicFilters.RABaseTriState("Вибратор", "el_867"));
        genres.add(new BasicFilters.RABaseTriState("Гарем", "el_87"));
        genres.add(new BasicFilters.RABaseTriState("Групповуха", "el_88"));
        genres.add(new BasicFilters.RABaseTriState("Гяру и гангуро", "el_844"));
        genres.add(new BasicFilters.RABaseTriState("Двойное проникновение", "el_911"));
        genres.add(new BasicFilters.RABaseTriState("Девочки волшебницы", "el_292"));
        genres.add(new BasicFilters.RABaseTriState("Дилдо", "el_868"));
        genres.add(new BasicFilters.RABaseTriState("Додзинси", "el_92"));
        genres.add(new BasicFilters.RABaseTriState("Домохозяйка", "el_300"));
        genres.add(new BasicFilters.RABaseTriState("Драма", "el_95"));
        genres.add(new BasicFilters.RABaseTriState("Жестокость", "el_883"));
        genres.add(new BasicFilters.RABaseTriState("Зоофилия", "el_94"));
        genres.add(new BasicFilters.RABaseTriState("Измена", "el_291"));
        genres.add(new BasicFilters.RABaseTriState("Изнасилование", "el_124"));
        genres.add(new BasicFilters.RABaseTriState("Инцест", "el_85"));
        genres.add(new BasicFilters.RABaseTriState("Исполнение желаний", "el_909"));
        genres.add(new BasicFilters.RABaseTriState("Исторический", "el_93"));
        genres.add(new BasicFilters.RABaseTriState("Камера", "el_869"));
        genres.add(new BasicFilters.RABaseTriState("Колготки", "el_849"));
        genres.add(new BasicFilters.RABaseTriState("Комедия", "el_73"));
        genres.add(new BasicFilters.RABaseTriState("Купальники", "el_845"));
        genres.add(new BasicFilters.RABaseTriState("Лоликон", "el_71"));
        genres.add(new BasicFilters.RABaseTriState("Маленькая грудь", "el_870"));
        genres.add(new BasicFilters.RABaseTriState("Мастурбация", "el_882"));
        genres.add(new BasicFilters.RABaseTriState("Много девушек", "el_860"));
        genres.add(new BasicFilters.RABaseTriState("Монстрдевушки", "el_671"));
        genres.add(new BasicFilters.RABaseTriState("На природе", "el_842"));
        genres.add(new BasicFilters.RABaseTriState("Научная фантастика", "el_76"));
        genres.add(new BasicFilters.RABaseTriState("Нетораре", "el_303"));
        genres.add(new BasicFilters.RABaseTriState("Огромный член", "el_884"));
        genres.add(new BasicFilters.RABaseTriState("Омораси", "el_81"));
        genres.add(new BasicFilters.RABaseTriState("Оральный секс", "el_853"));
        genres.add(new BasicFilters.RABaseTriState("Пайзури", "el_288"));
        genres.add(new BasicFilters.RABaseTriState("Парень пассив", "el_861"));
        genres.add(new BasicFilters.RABaseTriState("Пляж", "el_846"));
        genres.add(new BasicFilters.RABaseTriState("Повседневность", "el_90"));
        genres.add(new BasicFilters.RABaseTriState("Подчинение", "el_885"));
        genres.add(new BasicFilters.RABaseTriState("Психические отклонения", "el_886"));
        genres.add(new BasicFilters.RABaseTriState("Романтика", "el_74"));
        genres.add(new BasicFilters.RABaseTriState("Сверхестественное", "el_634"));
        genres.add(new BasicFilters.RABaseTriState("Секс игрушки", "el_871"));
        genres.add(new BasicFilters.RABaseTriState("Сётакон", "el_72"));
        genres.add(new BasicFilters.RABaseTriState("Сибари", "el_80"));
        genres.add(new BasicFilters.RABaseTriState("Спортивная форма", "el_891"));
        genres.add(new BasicFilters.RABaseTriState("Страпон", "el_872"));
        genres.add(new BasicFilters.RABaseTriState("Суккуб", "el_677"));
        genres.add(new BasicFilters.RABaseTriState("Темная Кожа", "el_611"));
        genres.add(new BasicFilters.RABaseTriState("Тентакли", "el_69"));
        genres.add(new BasicFilters.RABaseTriState("Трап", "el_859"));
        genres.add(new BasicFilters.RABaseTriState("Ужасы", "el_75"));
        genres.add(new BasicFilters.RABaseTriState("Учитель", "el_913"));
        genres.add(new BasicFilters.RABaseTriState("Учительница", "el_455"));
        genres.add(new BasicFilters.RABaseTriState("Фемдом", "el_873"));
        genres.add(new BasicFilters.RABaseTriState("Фистинг", "el_821"));
        genres.add(new BasicFilters.RABaseTriState("Фурри", "el_91"));
        genres.add(new BasicFilters.RABaseTriState("Футанари", "el_77"));
        genres.add(new BasicFilters.RABaseTriState("Фэнтези", "el_70"));
        genres.add(new BasicFilters.RABaseTriState("Цветной", "el_290"));
        genres.add(new BasicFilters.RABaseTriState("Цундере", "el_850"));
        genres.add(new BasicFilters.RABaseTriState("Чулки", "el_889"));
        genres.add(new BasicFilters.RABaseTriState("Школа", "el_86"));
        genres.add(new BasicFilters.RABaseTriState("Школьники", "el_874"));
        genres.add(new BasicFilters.RABaseTriState("Школьницы", "el_875"));
        genres.add(new BasicFilters.RABaseTriState("Шлюха", "el_763"));
        genres.add(new BasicFilters.RABaseTriState("Эксгибиционизм", "el_813"));
        genres.add(new BasicFilters.RABaseTriState("Эльфы", "el_286"));
        genres.add(new BasicFilters.RABaseTriState("Эччи", "el_798"));
        genres.add(new BasicFilters.RABaseTriState("Юри", "el_84"));
        genres.add(new BasicFilters.RABaseTriState("Яндере", "el_823"));
        genres.add(new BasicFilters.RABaseTriState("Яой", "el_83"));

        filterGroups.add(genres);
        filterGroups.add(categories);

    }

    @Override
    public List<FilterGroup> getFilters() {
        return filterGroups;
    }

    private class AllHentaiGenre extends Genre {

        private String link;

        private AllHentaiGenre(final String name, final String link) {
            super(name);
            this.link = link;
        }

        public String getLink() {
            return link;
        }

        public void setLink(final String link) {
            this.link = link;
        }
    }

    private List<Genre> genres = new ArrayList<>();

    {
        genres.add(new AllHentaiGenre("Все", "/list?sortType="));
        genres.add(new AllHentaiGenre("3D", "/list/genre/3D?lang=&sortType="));
        genres.add(new AllHentaiGenre("Ahegao", "/list/genre/ahegao?lang=&sortType="));
        genres.add(new AllHentaiGenre("Footfuck", "/list/genre/footfuck?lang=&sortType="));
        genres.add(new AllHentaiGenre("Gender bender", "/list/genre/gender_bender?lang=&sortType="));
        genres.add(new AllHentaiGenre("Mind break", "/list/genre/Mind+break?lang=&sortType="));
        genres.add(new AllHentaiGenre("Tomboy", "/list/genre/tomboy?lang=&sortType="));
        genres.add(new AllHentaiGenre("Анал", "/list/genre/anal?lang=&sortType="));
        genres.add(new AllHentaiGenre("Бакуню", "/list/genre/bakunyuu?lang=&sortType="));
        genres.add(new AllHentaiGenre("Бдсм", "/list/genre/bdsm?lang=&sortType="));
        genres.add(new AllHentaiGenre("Без цензуры", "/list/genre/no+censor?lang=&sortType="));
        genres.add(new AllHentaiGenre("Больница", "/list/genre/Hospital?lang=&sortType="));
        genres.add(new AllHentaiGenre("Большая грудь", "/list/genre/big+tits?lang=&sortType="));
        genres.add(new AllHentaiGenre("Буккакэ", "/list/genre/bukkake?lang=&sortType="));
        genres.add(new AllHentaiGenre("В ванной", "/list/genre/in+bathroom?lang=&sortType="));
        genres.add(new AllHentaiGenre("В общественном месте", "/list/genre/in+public+place?lang=&sortType="));
        genres.add(new AllHentaiGenre("В первый раз", "/list/genre/first+time?lang=&sortType="));
        genres.add(new AllHentaiGenre("Вибратор", "/list/genre/vibrator?lang=&sortType="));
        genres.add(new AllHentaiGenre("Гарем", "/list/genre/harem?lang=&sortType="));
        genres.add(new AllHentaiGenre("Групповуха", "/list/genre/gang+rape?lang=&sortType="));
        genres.add(new AllHentaiGenre("Гяру и гангуро", "/list/genre/gal+%26+gyaru?lang=&sortType="));
        genres.add(new AllHentaiGenre("Двойное проникновение", "/list/genre/double+penetration?lang=&sortType="));
        genres.add(new AllHentaiGenre("Девочки волшебницы", "/list/genre/Mahou+Shoujo?lang=&sortType="));
        genres.add(new AllHentaiGenre("Дилдо", "/list/genre/dildo?lang=&sortType="));
        genres.add(new AllHentaiGenre("Додзинси", "/list/genre/doujinshi?lang=&sortType="));
        genres.add(new AllHentaiGenre("Домохозяйка", "/list/genre/Housewife?lang=&sortType="));
        genres.add(new AllHentaiGenre("Драма", "/list/genre/drama?lang=&sortType="));
        genres.add(new AllHentaiGenre("Жестокость", "/list/genre/cruelty?lang=&sortType="));
        genres.add(new AllHentaiGenre("Зоофилия", "/list/genre/zoophilia?lang=&sortType="));
        genres.add(new AllHentaiGenre("Измена", "/list/genre/Betrayal?lang=&sortType="));
        genres.add(new AllHentaiGenre("Изнасилование", "/list/genre/rape?lang=&sortType="));
        genres.add(new AllHentaiGenre("Инцест", "/list/genre/incest?lang=&sortType="));
        genres.add(new AllHentaiGenre("Исполнение желаний", "/list/genre/granting+wish?lang=&sortType="));
        genres.add(new AllHentaiGenre("Исторический", "/list/genre/historical?lang=&sortType="));
        genres.add(new AllHentaiGenre("Камера", "/list/genre/camera?lang=&sortType="));
        genres.add(new AllHentaiGenre("Колготки", "/list/genre/pantyhose?lang=&sortType="));
        genres.add(new AllHentaiGenre("Комедия", "/list/genre/comedy?lang=&sortType="));
        genres.add(new AllHentaiGenre("Купальники", "/list/genre/swimmingsuit?lang=&sortType="));
        genres.add(new AllHentaiGenre("Лоликон", "/list/genre/lolicon?lang=&sortType="));
        genres.add(new AllHentaiGenre("Маленькая грудь", "/list/genre/small+tits?lang=&sortType="));
        genres.add(new AllHentaiGenre("Мастурбация", "/list/genre/masturbation?lang=&sortType="));
        genres.add(new AllHentaiGenre("Много девушек", "/list/genre/many+girls?lang=&sortType="));
        genres.add(new AllHentaiGenre("Монстрдевушки", "/list/genre/mostergirl?lang=&sortType="));
        genres.add(new AllHentaiGenre("На природе", "/list/genre/outside?lang=&sortType="));
        genres.add(new AllHentaiGenre("Научная фантастика", "/list/genre/science+fiction?lang=&sortType="));
        genres.add(new AllHentaiGenre("Нетораре", "/list/genre/Netorare?lang=&sortType="));
        genres.add(new AllHentaiGenre("Огромный член", "/list/genre/big+dick?lang=&sortType="));
        genres.add(new AllHentaiGenre("Омораси", "/list/genre/omorashi?lang=&sortType="));
        genres.add(new AllHentaiGenre("Оральный секс", "/list/genre/oral+sex?lang=&sortType="));
        genres.add(new AllHentaiGenre("Пайзури", "/list/genre/Paizuri?lang=&sortType="));
        genres.add(new AllHentaiGenre("Парень пассив", "/list/genre/passive+guy?lang=&sortType="));
        genres.add(new AllHentaiGenre("Пляж", "/list/genre/beach?lang=&sortType="));
        genres.add(new AllHentaiGenre("Повседневность", "/list/genre/slice+of+life?lang=&sortType="));
        genres.add(new AllHentaiGenre("Подчинение", "/list/genre/submission?lang=&sortType="));
        genres.add(new AllHentaiGenre("Психические отклонения", "/list/genre/mental+illness?lang=&sortType="));
        genres.add(new AllHentaiGenre("Романтика", "/list/genre/romance?lang=&sortType="));
        genres.add(new AllHentaiGenre("Сверхестественное", "/list/genre/supernatural?lang=&sortType="));
        genres.add(new AllHentaiGenre("Секс игрушки", "/list/genre/sex+toys?lang=&sortType="));
        genres.add(new AllHentaiGenre("Сётакон", "/list/genre/shoutacon?lang=&sortType="));
        genres.add(new AllHentaiGenre("Сибари", "/list/genre/shibari?lang=&sortType="));
        genres.add(new AllHentaiGenre("Спортивная форма", "/list/genre/sports+uniform?lang=&sortType="));
        genres.add(new AllHentaiGenre("Страпон", "/list/genre/strapon?lang=&sortType="));
        genres.add(new AllHentaiGenre("Суккуб", "/list/genre/Succubus?lang=&sortType="));
        genres.add(new AllHentaiGenre("Темная Кожа", "/list/genre/Dark+Skin?lang=&sortType="));
        genres.add(new AllHentaiGenre("Тентакли", "/list/genre/tentacles?lang=&sortType="));
        genres.add(new AllHentaiGenre("Трап", "/list/genre/trap?lang=&sortType="));
        genres.add(new AllHentaiGenre("Ужасы", "/list/genre/horror?lang=&sortType="));
        genres.add(new AllHentaiGenre("Учитель", "/list/genre/Male+School+teacher?lang=&sortType="));
        genres.add(new AllHentaiGenre("Учительница", "/list/genre/Female+school+teacher?lang=&sortType="));
        genres.add(new AllHentaiGenre("Фемдом", "/list/genre/femdom?lang=&sortType="));
        genres.add(new AllHentaiGenre("Фистинг", "/list/genre/fisting?lang=&sortType="));
        genres.add(new AllHentaiGenre("Фурри", "/list/genre/furry?lang=&sortType="));
        genres.add(new AllHentaiGenre("Футанари", "/list/genre/futanary?lang=&sortType="));
        genres.add(new AllHentaiGenre("Фэнтези", "/list/genre/fantasy?lang=&sortType="));
        genres.add(new AllHentaiGenre("Цветной", "/list/genre/Colorful?lang=&sortType="));
        genres.add(new AllHentaiGenre("Цундере", "/list/genre/tsundere?lang=&sortType="));
        genres.add(new AllHentaiGenre("Чулки", "/list/genre/hose?lang=&sortType="));
        genres.add(new AllHentaiGenre("Школа", "/list/genre/school?lang=&sortType="));
        genres.add(new AllHentaiGenre("Школьники", "/list/genre/school+boys?lang=&sortType="));
        genres.add(new AllHentaiGenre("Школьницы", "/list/genre/school+girls?lang=&sortType="));
        genres.add(new AllHentaiGenre("Шлюха", "/list/genre/slut?lang=&sortType="));
        genres.add(new AllHentaiGenre("Эксгибиционизм", "/list/genre/exhibitionism?lang=&sortType="));
        genres.add(new AllHentaiGenre("Эльфы", "/list/genre/Elfs?lang=&sortType="));
        genres.add(new AllHentaiGenre("Эччи", "/list/genre/ecchi?lang=&sortType="));
        genres.add(new AllHentaiGenre("Юри", "/list/genre/yuri?lang=&sortType="));
        genres.add(new AllHentaiGenre("Яндере", "/list/genre/yandere?lang=&sortType="));
        genres.add(new AllHentaiGenre("Яой", "/list/genre/yaoi?lang=&sortType="));
    }

    @Override
    public List<Genre> getGenres() {
        return genres;
    }

}
