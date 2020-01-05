package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.interfaces.controllers.GameControllerIntf;
import com.onyshchenko.psanalyzer.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    @Autowired
    private GameControllerIntf gameController;

    public Game checkGameAlreadyExists(String name) {
        return gameController.findByName(name);
    }

    public void checkList(List<Game> games) {

        logger.info("Checking list of games");

        if (games.isEmpty()) return;

        for (Game game : games) {
            String name = game.getName();
            logger.info("Checking game record for name: " + name + ".");
            Game gameFromDb = checkGameAlreadyExists(name);
            if (gameFromDb == null) {
                logger.info("Creating game record.");
                gameController.createGame(game);
            } else {
                if (!game.getPrice().equals(gameFromDb.getPrice())) {
                    logger.info("Updating game price.");
                    gameController.updateGame(gameFromDb.getId(), game);
                }
                logger.info("Actual game already exists in DB.");
            }
        }
    }
}
