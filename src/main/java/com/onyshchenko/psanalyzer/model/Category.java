package com.onyshchenko.psanalyzer.model;

import java.util.HashMap;
import java.util.Map;

public enum Category {

    FULL("FULL_GAME", "Full Game", "Полная версия игры"),
    PREMIUM("PREMIUM_EDITION", "Premium Edition", "Премиум-издание"),
    SOUNDTRACK("SOUNDTRACK", null, "Звуковое сопровождение"),
    GAME_BUNDLE("GAME_BUNDLE", "Game Bundle", "Игровой комплект"),
    BUNDLE("BUNDLE", "Bundle", "Комплект"),
    OTHER("OTHER", "Add-on", "Дополнение"),
    OTHER_PACK("ADD-ON_PACK", "Add-On Pack", "Пакет дополнений"),
    LEVEL("LEVEL", "Level", "Уровень"),
    CHARACTER("CHARACTER", "Character", "Персонаж"),
    MAP("MAP", "Map", "Пакет карт"),
    SEASON_PASS("SEASON_PASS", "Season Pass", "Сезонный пропуск"),
    VEHICLE("VEHICLE", "Vehicle", "Автомобиль"),
    DEMO("DEMO", "Demo", "Демо-версия"),
    ITEM("ITEM", "Item", "Объект"),
    VIRTUAL_CURRENCY("VIRTUAL_CURRENCY", "Virtual Currency", "Виртуальная валюта"),
    COSTUME("COSTUME", "COSTUME", "Костюм"),
    EPISODE("EPISODE", "Episode", "Эпизод"),
    WEAPONS("WEAPONS", "Weapons", "Оружие");

    private static final Map<String, Category> ruUaCategoryName = new HashMap<>(values().length, 1);

    static {
        for (Category category : values()) {
            ruUaCategoryName.put(category.ruUaName, category);
        }
    }

    private final String key;
    private final String originalName;
    private final String ruUaName;

    Category(String key, String originalName, String ruUaName) {
        this.key = key;
        this.originalName = originalName;
        this.ruUaName = ruUaName;
    }

    public String getCategoryRuUaName() {
        return ruUaName;
    }

    public static Category ofRuUaName(String categoryName) {
        Category result = ruUaCategoryName.get(categoryName);
        if (result == null) {
            throw new IllegalArgumentException("Invalid category name: " + categoryName);
        }
        return result;
    }

    public String getKey() {
        return key;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getRuUaName() {
        return ruUaName;
    }
}
