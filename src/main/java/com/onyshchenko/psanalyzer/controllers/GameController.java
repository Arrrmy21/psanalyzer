package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.interfaces.controllers.GameControllerIntf;
import com.onyshchenko.psanalyzer.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class GameController implements GameControllerIntf {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameRepository gameRepository;

    public Optional<Game> getGame(@PathVariable(value = "id") String id) {
        logger.info("Trying to get game by id from repository.");
        return gameRepository.findById(id);
    }

    @Override
    public Page<Game> getGames(int page, int size) {
        logger.info("Trying to get list of games from repository with params: page= " + page + "; size= " + size);
        return gameRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public ResponseEntity<Object> createGame(Game game) {
        logger.info("Trying to create game in repository.");
        gameRepository.save(game);
        return new ResponseEntity<>("Game created.", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> updateGame(Game gameDetails) {
        logger.info("Trying to update the game on repository.");

        Game game = gameRepository.findById(gameDetails.getId()).orElseThrow(
                () -> new IllegalArgumentException("Game not found id: " + gameDetails.getId()));

        gameRepository.save(gameDetails);
        return new ResponseEntity<>("Game updated.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> deleteGame(@PathVariable String id) {
        logger.info("Trying to delete the game from repository.");
        Game game = gameRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Game not found id: " + id));
        gameRepository.delete(game);
        return new ResponseEntity<>("Game deleted.", HttpStatus.OK);
    }

}
