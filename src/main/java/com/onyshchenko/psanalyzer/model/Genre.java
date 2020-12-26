package com.onyshchenko.psanalyzer.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

public enum Genre {

    ACTION("Action"),
    Adventure("Приключения"),
    ARCADE("Аркада"),
    EDUCATIONAL("Образовательные"),
    FAMILY("Семейные"),
    FIGHTING("Единоборства"),
    FPS("Боевики"),
    FITNESS("Фитнес"),
    HORROR("Ужасы"),
    MUSIC("Музыка и ритм"),
    PARTY("Тусовка"),
    PLATFORMER("Казуальные"),
    PUZZLE("Пазлы"),
    RACING("Вождение и гонки"),
    RPG("Ролевые игры"),
    SHOOTER("Шутеры"),
    SIMULATION("Симуляторы"),
    IMITATION("Имитация"),
    SPORTS("Спорт"),
    STRATEGY("Стратегии"),
    QUEST("Головоломки"),
    BRAIN_TRAINING("Тренировка мозга"),
    ADULT("Для взрослых"),
    UNIQUE("Уникальные");

    private static Map<String, Genre> map = new HashMap<>(values().length, 1);

    static {
        for (Genre genre : values()) {
            map.put(genre.genreName, genre);
        }
    }

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "genre_name")
    private String genreName;

    Genre(Long id, String genreName) {
        this.id = id;
        this.genreName = genreName;
    }

    Genre(String genreName) {
        this.genreName = genreName;
    }

    public String getGenreName() {
        return genreName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static Genre of(String genreName) {
        Genre result = map.get(genreName);
        if (result == null) {
            throw new IllegalArgumentException("Invalid genre name [" + genreName + "]");
        }
        return result;
    }
}