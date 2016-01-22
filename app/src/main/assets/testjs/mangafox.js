var TAG = "MangaFoxEngine";

/**
***
*** $ - объект, содержащий в себе полезные функции
*** $.get(url: строка): строка - отправка GET-запроса, возвращает строку ответа
*** $.loge(tag: строка, error: строка) и $.logi(tag: строка, info: строка) - методы для логирования
***     loge - ошибки, logi - информация
***
**/

function loge(a,b){
    $.loge(a,b)
}

function logi(a,b){
    $.logi(a,b)
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

