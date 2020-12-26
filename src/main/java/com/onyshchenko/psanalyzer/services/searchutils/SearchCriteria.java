package com.onyshchenko.psanalyzer.services.searchutils;

import com.onyshchenko.psanalyzer.model.RequestFilters;

public class SearchCriteria {
    private RequestFilters key;
    private String operation;
    private Object value;

    public SearchCriteria(RequestFilters key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public RequestFilters getKey() {
        return key;
    }

    public void setKey(RequestFilters key) {
        this.key = key;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
