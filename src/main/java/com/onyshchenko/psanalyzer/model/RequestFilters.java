package com.onyshchenko.psanalyzer.model;

public enum RequestFilters {

    PRICE("price"),
    NAME("searchName"),
    PUBLISHER("searchPublisher"),
    RELEASE("releaseDate"),
    DISCOUNT("discount"),
    USERID("userId");

    RequestFilters(String filter) {
        this.filterName = filter;
    }

    private final String filterName;

    public String getFilterName() {
        return filterName;
    }
}
