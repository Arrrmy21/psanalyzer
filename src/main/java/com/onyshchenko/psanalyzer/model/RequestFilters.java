package com.onyshchenko.psanalyzer.model;

public enum RequestFilters {

    PRICE("price"),
    NAME("searchName"),
    PUBLISHER("searchPublisher"),
    PUBLISHER_ID("id"),
    RELEASE("releaseDate"),
    DISCOUNT("discount"),
    GENRE("genre"),
    CATEGORY("category"),
    PSPLUS("psplus"),
    EAACCESS("isEaAccess"),
    EXCLUSIVE("isExclusive"),
    TEST1("isExclusive"),
    Test2("isExclusive"),
    USERID("userId"),
    PAGE("page");

    RequestFilters(String filter) {
        this.filterName = filter;
    }

    private final String filterName;

    public String getFilterName() {
        return filterName;
    }
}
