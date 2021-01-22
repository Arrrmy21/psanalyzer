package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.controllers.interfaces.GameControllerIntf;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.services.FilteringUtils;
import com.onyshchenko.psanalyzer.services.GameService;
import com.onyshchenko.psanalyzer.services.HtmlHookService;
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
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class GameController implements GameControllerIntf {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameService gameService;
    @Autowired
    private FilteringUtils filteringUtils;
    @Autowired
    private HtmlHookService htmlHookService;

    public Optional<Game> getGame(@PathVariable(value = "gameId") String gameId) {
        LOGGER.info("Getting game by id [{}] from repository.", gameId);

        return gameService.getGame(gameId);
    }

    @Override
    public Optional<Game> getPersonalizedGame(String gameId, long userId) {
        LOGGER.info("Getting PERSONALIZED game by id [{}] from repository.", gameId);

        return gameService.getPersonalizedGame(gameId, userId);
    }

    @Override
    public Page<Game> getGames(int page, int size, String filter) throws ValidationException {
        LOGGER.info("Getting list of games from repository with params: page=[{}], size=[{}]", page, size);
        return gameService.getListOfGames(PageRequest.of(page, size), filter);
    }

    @Override
    public Page<Game> getPersonalizedGames(int page, int size, String filter, long userId) throws ValidationException {
        LOGGER.info("Getting PERSONALIZED list of games from repository with params: page=[{}], size=[{}]", page, size);
        return gameService.getPersonalizedListOfGames(PageRequest.of(page, size), filter, userId);
    }

    @Override
    public ResponseEntity<Object> createGame(Game game) {
        LOGGER.info("Trying to create game in repository.");
        gameRepository.save(game);
        return new ResponseEntity<>("Game created.", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> updateGame(Game gameDetails) {
        LOGGER.info("Trying to update the game on repository.");

        if (!gameRepository.findById(gameDetails.getId()).isPresent()) {
            throw new IllegalArgumentException("Game not found id: " + gameDetails.getId());
        }

        gameRepository.save(gameDetails);
        return new ResponseEntity<>("Game updated.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> deleteGame(@PathVariable String id) {
        LOGGER.info("Trying to delete the game from repository.");
        Game game = gameRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Game not found id: " + id));
        gameRepository.delete(game);
        return new ResponseEntity<>("Game deleted.", HttpStatus.OK);
    }

    @Override
    public void startUpdateGameProcedure() {
        LOGGER.info("Starting collecting data via controller.");
        htmlHookService.collectMinimalDataAboutGamesScheduledTask();
        LOGGER.info("Collecting data via controller FINISHED.");
    }
}
