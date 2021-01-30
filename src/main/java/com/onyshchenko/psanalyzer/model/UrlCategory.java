package com.onyshchenko.psanalyzer.model;

public enum UrlCategory {

    ALL_GAMES("category/44d8bb20-653e-431e-8ad0-c0a365f68d2f/", Category.GAME),
    SALES("category/803cee19-e5a1-4d59-a463-0b6b2701bf7c/", Category.DEFAULT),
    VR("category/95239ca7-2dcf-43d9-8d4b-b7672ee9304a/", Category.PSVR),
    PS5("category/4cbf39e2-5749-4970-ba81-93a489e4570c/", Category.DEFAULT),
    EXCLUSIVE("https://www.playstation.com/ru-ua/ps4/ps4-games/ps4-exclusives/", Category.EXCLUSIVE);


    private final String searchingUrl;
    private final Category category;

    UrlCategory(String searchingUrl, Category category) {
        this.searchingUrl = searchingUrl;
        this.category = category;
    }

    public String getUrl() {
        return searchingUrl;
    }

    public Category getCategory() {
        return category;
    }
}
