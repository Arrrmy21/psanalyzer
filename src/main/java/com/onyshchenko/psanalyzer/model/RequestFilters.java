package com.onyshchenko.psanalyzer.model;

public enum RequestFilters {

    PRICE("price"),
    NAME("searchName"),
    USERID("userId");

    RequestFilters(String filter) {
        this.filterName = filter;
    }

    private String filterName;

    public String getFilterName() {
        return filterName;
    }
}
