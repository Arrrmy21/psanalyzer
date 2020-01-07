package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.dao.GameRepository;
import com.onyshchenko.psanalyzer.interfaces.controllers.GameControllerIntf;
import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
public class GameController implements GameControllerIntf {

    @Autowired
    private GameRepository gameRepository;

    public ResponseEntity<Game> getGame(@PathVariable(value = "id") String id) {
        Game game = gameRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Game not found id ::" + id));
        return ResponseEntity.ok().body(game);
    }

    @Override
    public Game findByName(@PathVariable(value = "name") String name) {
        return gameRepository.findByName(name);
    }

    @Override
    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    @Override
    public ResponseEntity<Object> createGame(Game game) {
        gameRepository.save(game);
        return new ResponseEntity<>("Game created.", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> updateGame(String id, Game gameDetails) {
        Game game = gameRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Game not found id ::" + id));
        gameRepository.save(gameDetails);
        return new ResponseEntity<>("Game updated", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> deleteGame(@PathVariable String id) {
        Game game = gameRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Game not found id ::" + id));
        gameRepository.delete(game);
        return new ResponseEntity<>("Game deleted", HttpStatus.OK);
    }

}
