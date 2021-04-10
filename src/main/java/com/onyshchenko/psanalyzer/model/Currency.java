package com.onyshchenko.psanalyzer.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Currency {

    UAH("UAH");

    private static final Map<String, Currency> map = new HashMap<>(values().length, 1);

    static {
        for (Currency currency : values()) {
            map.put(currency.getCurrencyName(), currency);
        }
    }

    private final String currencyName;

    Currency(String name) {
        currencyName = name;
    }

    public static Currency of(String currencyName) {
        Currency result = map.get(currencyName);
        if (result == null) {
            throw new IllegalArgumentException("Invalid currency name: " + currencyName);
        }
        return result;
    }
}
