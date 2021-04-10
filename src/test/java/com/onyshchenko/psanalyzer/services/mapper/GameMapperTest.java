package com.onyshchenko.psanalyzer.services.mapper;

import com.onyshchenko.psanalyzer.model.Category;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Genre;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameMapperTest {

    private static final long GAME_ID = 123;
    private static final String NEW_GAME_NAME = "new-game-Name-123";

    private GameMapper gameMapper = Mappers.getMapper(GameMapper.class);

    @Test
    public void testGameMapperWorksCorrect() {

        Game defaultGame = prepareDefaultGame();
        Game entityWithNewValues = prepareNewEntity();

        gameMapper.updateGameData(entityWithNewValues, defaultGame);

        assertEquals(NEW_GAME_NAME, defaultGame.getName());
        assertNotNull(defaultGame.getId());
        assertNotNull(defaultGame.getGenres());
        assertNotNull(defaultGame.getUrl());
        assertNotNull(defaultGame.getCategory());
        assertFalse(defaultGame.getErrorWhenFilling());
    }

    private Game prepareDefaultGame() {
        Game game = new Game();
        game.setId(GAME_ID);
        game.setName("default-Name");
        game.setGenres(new HashSet<>(Collections.singleton(Genre.ACTION)));

        return game;
    }

    private Game prepareNewEntity() {
        Game game = new Game();

        game.setUrl("new-game-Url-123");
        game.setCategory(Category.LEVEL);
        game.setName(NEW_GAME_NAME);
        game.setErrorWhenFilling(false);

        return game;
    }
}
