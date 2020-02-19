package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    @Autowired
    private PriceService priceService;
    @Autowired
    private GameRepository gameRepository;

    public Optional<Game> checkGameAlreadyExists(String id) {
        return gameRepository.findById(id);
    }

    public void checkList(List<Game> games) {

        logger.info("Checking list of games.");

        if (games.isEmpty()) {
            logger.info("List of games is empty.");
            return;
        }

        for (Game game : games) {
            logger.info("Checking game record for name: " + game.getName() + ".");
            Optional<Game> gameFromDb = checkGameAlreadyExists(game.getId());
            if (!gameFromDb.isPresent()) {
                logger.info("Creating game record.");
                gameRepository.save(game);
            } else {
                if (game.getPrice().getCurrentPrice() != gameFromDb.get().getPrice().getCurrentPrice()) {
                    logger.info("Checking game price.");
                    gameFromDb.get().setPrice(priceService.updatePriceComparingWithExisting(game.getPrice(), gameFromDb.get().getPrice()));
                    gameRepository.save(gameFromDb.get());
                } else logger.info("Actual game already exists in DB.");
            }
        }
    }
}
