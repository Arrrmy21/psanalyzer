package com.onyshchenko.psanalyzer.model;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum Genre {

    ACTION("ACTION", "Action", "Боевики"),
    ADVENTURE("ADVENTURE", "Adventure", "Приключения"),
    ARCADE("ARCADE", "Arcade", "Аркада"),
    SHOOTER("SHOOTER", "Shooter", "Шутеры"),
    RPG("ROLE_PLAYING_GAMES", "Role Playing Games", "Ролевые игры"),
    CASUAL("CASUAL", "Casual", "Казуальные"),
    UNIQUE("UNIQUE", "Unique", "Уникальные"),
    PUZZLE("PUZZLE", "Puzzle", "Головоломки"),
    SIMULATION("SIMULATION", "Simulation", "Имитация"),
    HORROR("HORROR", "Horror", "Ужасы"),
    RACING("RACING", "Driving/Racing", "Вождение и гонки"),
    SPORTS("SPORTS", "Sport", "Спорт"),
    FIGHTING("FIGHTING", "Fighting", "Единоборства"),
    STRATEGY("STRATEGY", "Strategy", "Стратегии"),
    FAMILY("FAMILY", "Family", "Семейные"),
    MUSIC("MUSIC/RHYTHM", "Music/Rhythm", "Музыка и ритм"),
    PARTY("PARTY", "Party", "Тусовка"),
    SIMULATOR("SIMULATOR", "Simulator", "Симуляторы"),
    EDUCATIONAL("EDUCATIONAL", "Educational", "Образовательные"),
    BRAIN_TRAINING("BRAIN_TRAINING", null, "Тренировка мозга"),
    FITNESS("FITNESS", null, "Фитнес"),
    ADULT("ADULT", null, "Для взрослых"),
    QUIZ("QUIZ", null, "Викторины");

    private static Map<String, Genre> ruUaGenreNameMap = new HashMap<>(values().length, 1);

    static {
        for (Genre genre : values()) {
            ruUaGenreNameMap.put(genre.genreRuUaName, genre);
        }
    }

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "genre_name")
    private final String genreRuUaName;
    private final String key;
    private final String originalName;

    Genre(String key, String originalName, String genreRuUaName) {
        this.key = key;
        this.originalName = originalName;
        this.genreRuUaName = genreRuUaName;
    }

    public static Genre ofRuUaName(String genreName) {
        Genre result = ruUaGenreNameMap.get(genreName);
        if (result == null) {
            throw new IllegalArgumentException("Invalid genre name [" + genreName + "]");
        }
        return result;
    }
}