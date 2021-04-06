package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.controllers.interfaces.GameControllerIntf;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Publisher;
import com.onyshchenko.psanalyzer.services.FilteringUtils;
import com.onyshchenko.psanalyzer.services.GameService;
import com.onyshchenko.psanalyzer.services.PublisherService;
import com.onyshchenko.psanalyzer.services.scheduler.ScheduledTasksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.ValidationException;
import java.util.Optional;
import java.util.List;

@RestController
public class GameController implements GameControllerIntf {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameService gameService;
    @Autowired
    private FilteringUtils filteringUtils;
    @Autowired
    private ScheduledTasksService scheduledTasksService;
    @Autowired
    private PublisherService publisherService;

    public Optional<Game> getGame(@PathVariable(value = "gameId") long gameId) {
        LOGGER.info("Get game request. Id: [{}]", gameId);

        return gameService.getGame(gameId);
    }

    @Override
    public Optional<Game> getPersonalizedGame(long gameId, long userId) {
        LOGGER.info("Get PERSONALIZED game by id request. Id: [{}].", gameId);

        return gameService.getPersonalizedGame(gameId, userId);
    }

    @Override
    public Page<Game> getGames(int page, int size, String filter) throws ValidationException {
        LOGGER.info("Get list of games request. Page=[{}], size=[{}], filter=[{}]", page, size, filter);

        return gameService.getListOfGames(PageRequest.of(page, size), filter);
    }

    @Override
    public Page<Game> getPersonalizedGames(int page, int size, String filter, long userId) throws ValidationException {
        LOGGER.info("Get PERSONALIZED list of games request. Params: UserId=[{}], page=[{}], size=[{}], filter=[{}]",
                userId, page, size, filter);

        return gameService.getPersonalizedListOfGames(PageRequest.of(page, size), filter, userId);
    }

    @Override
    public ResponseEntity<Object> createGame(Game game) {
        LOGGER.info("Create game request. Game name: [{}]", game.getName());
        Optional<Game> createdGame = gameService.createGame(game);
        if (createdGame.isPresent()) {
            return new ResponseEntity<>("Game created.", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Error on creating of game.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Object> updateGame(Game gameDetails) {
        LOGGER.info("Trying to update the game on repository.");

        Optional<Game> gameFromDb = gameService.getGameByID(gameDetails.getId());

        if (!gameFromDb.isPresent()) {
            return new ResponseEntity<>("Game not found id: [" + gameDetails.getId() + "].", HttpStatus.BAD_REQUEST);
        } else {
            gameService.saveGameRecordIntoDb(gameDetails);
            return new ResponseEntity<>("Game updated.", HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Object> deleteGame(@PathVariable long id) {
        LOGGER.info("Trying to delete the game from repository.");

        return gameService.deleteGame(id);
    }

    @Override
    public void startUpdateGameProcedure() {
        LOGGER.info("Starting collecting data via controller.");
        scheduledTasksService.collectDataAboutGamesByList();
        LOGGER.info("Collecting data via controller FINISHED.");
    }

    @Override
    public Page<Publisher> getListOfAllPublishers(int page, int size) {
        LOGGER.info("Get list of all publishers request. Page=[{}], size=[{}]", page, size);

        return publisherService.getListOfAllPublishers(PageRequest.of(page, size));
    }
}
