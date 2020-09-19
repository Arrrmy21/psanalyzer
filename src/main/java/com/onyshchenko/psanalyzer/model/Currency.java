package com.onyshchenko.psanalyzer.model;

public enum Currency {

    UAH("UAH");

    private String priceCurrency;

    Currency(String name) {
        priceCurrency = name;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }
}
