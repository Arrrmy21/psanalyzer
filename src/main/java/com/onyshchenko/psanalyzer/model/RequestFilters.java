package com.onyshchenko.psanalyzer.model;

public enum RequestFilters {

    PRICE("price"),
    NAME("name");

    RequestFilters(String filter) {
        this.filterName = filter;
    }

    private String filterName;

    public String getFilterName() {
        return filterName;
    }
}
