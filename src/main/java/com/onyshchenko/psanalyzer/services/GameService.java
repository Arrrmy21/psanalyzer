package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.interfaces.GameServiceIntf;
import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;


@RestController
public class GameService implements GameServiceIntf {

    @Autowired
    private GameRepository gameRepository;

    public ResponseEntity<Game> getGame(@PathVariable(value = "id") String id) {
        Game game = gameRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Game not found od ::" + id));
        return ResponseEntity.ok().body(game);
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
                () -> new NoSuchElementException("Game not found od ::" + id));
        game.setName(gameDetails.getName());
        gameRepository.save(game);
        return new ResponseEntity<>("Game updated", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> deleteGame(@PathVariable String id) {
        Game game = gameRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Game not found od ::" + id));
        gameRepository.delete(game);
        return new ResponseEntity<>("Game deleted", HttpStatus.OK);
    }

}
