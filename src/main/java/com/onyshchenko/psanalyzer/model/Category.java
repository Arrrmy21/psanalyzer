package com.onyshchenko.psanalyzer.model;

import java.util.HashMap;
import java.util.Map;

public enum Category {

    FULL("Полная версия"),
    KIT("Комплект"),
    LEVEL("Уровень"),
    DLC("Дополнение"),
    PSN("Игра PSN"),
    PSVR("Игра PS VR"),
    MAP("Пакет карт"),
    THEME("Тема"),
    DYNTHEME("Динамическая тема"),
    GAME("Игра"),
    AVATAR("Аватар"),
    DEFAULT("Default"),
    EXCLUSIVE("Exclusive");

    private static final Map<String, Category> map = new HashMap<>(values().length, 1);

    static {
        for (Category category : values()) {
            map.put(category.categoryName, category);
        }
    }

    private final String categoryName;

    Category(String name) {
        categoryName = name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public static Category of(String categoryName) {
        Category result = map.get(categoryName);
        if (result == null) {
            throw new IllegalArgumentException("Invalid category name: " + categoryName);
        }
        return result;
    }
}
