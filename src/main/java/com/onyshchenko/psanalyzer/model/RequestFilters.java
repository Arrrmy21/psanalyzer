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
    //TODO: Revert this changes
    GERRIT_FILTER("gerrit"),
    USERID("userId");

    RequestFilters(String filter) {
        this.filterName = filter;
    }

    private final String filterName;

    public String getFilterName() {
        return filterName;
    }
}
