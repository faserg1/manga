var TAG = "MangaFoxEngine";

/**
***
*** $ - объект, содержащий в себе полезные функции из Java
*** $.get(url: строка): строка - отправка GET-запроса, возвращает строку ответа
*** $.loge(tag: строка, error: строка) и $.logi(tag: строка, info: строка) - методы для логирования
***     loge - ошибки, logi - информация
***
**/

function loge(a,b){
    $.loge(a.toString(),b.toString())
}

function logi(a,b){
    $.logi(a.toString(),b.toString())
}

function getSuggestions(query) {
    var suggestionsArray = [];

    var baseUrl = "http://mangafox.me/manga/"
    var coverBaseUrl = "http://www.mangafox.com/icon/"
    var suggestions = JSON.parse($.get("http://mangafox.me/ajax/search.php?term=" + query));
    for (var i = 0; i < suggestions.length; i++) {
        var suggestion = suggestions[i];
        var mangaUrl = baseUrl + suggestion[2];
        var mangaSuggestion = {
            mangaTitle: suggestion[1],
            mangaUrl: mangaUrl,
            mangaCover: coverBaseUrl + suggestion[0] + ".jpg"
        };
        suggestionsArray.push(mangaSuggestion);
    }

    return suggestionsArray;
}

function queryRepository(query) {
    var baseCoverUrl = "http://c.mfcdn.net/store/manga/"
    var baseSearchUri = "http://mangafox.me/search.php?name_method=cw&type=&author_method=cw&author=&artist_method=cw&artist=&genres%5BAction%5D=0&genres%5BAdult%5D=0&genres%5BAdventure%5D=0&genres%5BComedy%5D=0&genres%5BDoujinshi%5D=0&genres%5BDrama%5D=0&genres%5BEcchi%5D=0&genres%5BFantasy%5D=0&genres%5BGender+Bender%5D=0&genres%5BHarem%5D=0&genres%5BHistorical%5D=0&genres%5BHorror%5D=0&genres%5BJosei%5D=0&genres%5BMartial+Arts%5D=0&genres%5BMature%5D=0&genres%5BMecha%5D=0&genres%5BMystery%5D=0&genres%5BOne+Shot%5D=0&genres%5BPsychological%5D=0&genres%5BRomance%5D=0&genres%5BSchool+Life%5D=0&genres%5BSci-fi%5D=0&genres%5BSeinen%5D=0&genres%5BShoujo%5D=0&genres%5BShoujo+Ai%5D=0&genres%5BShounen%5D=0&genres%5BShounen+Ai%5D=0&genres%5BSlice+of+Life%5D=0&genres%5BSmut%5D=0&genres%5BSports%5D=0&genres%5BSupernatural%5D=0&genres%5BTragedy%5D=0&genres%5BWebtoons%5D=0&genres%5BYaoi%5D=0&genres%5BYuri%5D=0&released_method=eq&released=&rating_method=eq&rating=&is_completed=&advopts=1&name="
    var url = baseSearchUri + query;

    var doc = $.parseDoc($.get(url));
    var mangaArray = [];

    var results = doc.select("#listing tr");

    for (var i = 1; i < results.size(); i++) { //первая строка - голова таблицы
        var result = results.get(i);
        var seriesPreview = result.select(".series_preview");

        var mangaUrl = seriesPreview.attr("href");
        var mangaCoverId = seriesPreview.attr("rel");
        var mangaTitle = seriesPreview.text();
        mangaArray.push({
            mangaTitle: mangaTitle,
            mangaUrl: mangaUrl,
            mangaCover: baseCoverUrl + mangaCoverId + "/cover.jpg"
        });
    }
    return mangaArray;
}

function __getChapters(doc) {
    return doc.select("#chapters div h4");
}

function queryForMangaDescription(mangaUrl) {
    var doc = $.parseDoc($.get(mangaUrl));
    var description = doc.select(".summary").text();
    var titleBlocks = doc.select("#title > table td");
    var mangaAuthor = titleBlocks.get(1).text();
    var genres = titleBlocks.get(3).text();

    var chapters = __getChapters(doc);
    var chaptersQuantity = chapters.size();
    return {
        mangaDescription: description,
        mangaAuthor: mangaAuthor,
        mangaGenres: genres,
        chaptersQuantity: chaptersQuantity
    };
}

function queryMangaForChaptersPROP() {
    var chapters = __getChapters(doc);
    for (var i = 0; i < chapters.size(); i++) {
        var chapter = chapters.get(i);
        var chapterTitle = chapter.text();
    }
}

