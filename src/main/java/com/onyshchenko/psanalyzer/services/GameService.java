package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.interfaces.GameServiceIntf;
import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@RestController
public class GameService implements GameServiceIntf {

    private static Map<String, Game> database = new HashMap<>();

    static {
        Game game1 = new Game("1", "game1Name");
        database.put(game1.getId(), game1);
    }

    public Game getGame(@PathVariable String id) {
        return database.get(id);
    }

    @Override
    public Collection<Game> getGames() {
        return database.values();
    }

    @Override
    public ResponseEntity<Object> createGame(Game game) {
        database.put(game.getId(), game);
        return new ResponseEntity<>("Game created.", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> updateGame(String id, Game game) {
        database.remove(id);
        game.setId(id);
        database.put(id, game);
        return new ResponseEntity<>("Game updated", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> deleteGame(@PathVariable String id) {
        database.remove(id);
        return new ResponseEntity<>("Game deleted", HttpStatus.OK);
    }

}
