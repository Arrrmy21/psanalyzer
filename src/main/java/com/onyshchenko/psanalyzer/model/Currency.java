package com.onyshchenko.psanalyzer.model;

import java.util.HashMap;
import java.util.Map;

public enum Currency {

    UAH("UAH");

    private static Map<String, Currency> map = new HashMap<>(values().length, 1);

    static {
        for (Currency currency : values()) {
            map.put(currency.getCurrencyName(), currency);
        }
    }

    private String currencyName;

    Currency(String name) {
        currencyName = name;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public static Currency of(String currencyName) {
        Currency result = map.get(currencyName);
        if (result == null) {
            throw new IllegalArgumentException("Invalid currency name: " + currencyName);
        }
        return result;
    }
}
