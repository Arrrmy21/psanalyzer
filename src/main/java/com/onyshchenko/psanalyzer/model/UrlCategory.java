package com.onyshchenko.psanalyzer.model;

public enum UrlCategory {

    PS4("44d8bb20-653e-431e-8ad0-c0a365f68d2f"),
    ALL_SALES("803cee19-e5a1-4d59-a463-0b6b2701bf7c"),
    VR("95239ca7-2dcf-43d9-8d4b-b7672ee9304a"),
    PS5("4cbf39e2-5749-4970-ba81-93a489e4570c"),
    SALES("6caa0197-46cd-4a95-8318-b8a0f1bd5a0a"),
    FREE("5c30b111-b867-4037-8f42-5b3db18d8e20"),
    NEW("12a53448-199e-459b-956d-074feeed2d7d");


    private final String searchingUrl;

    UrlCategory(String searchingUrl) {
        this.searchingUrl = searchingUrl;
    }

    public String getUrl() {
        return searchingUrl;
    }

}
