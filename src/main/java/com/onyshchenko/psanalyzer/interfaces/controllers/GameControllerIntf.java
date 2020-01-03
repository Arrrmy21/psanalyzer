package com.onyshchenko.psanalyzer.interfaces.controllers;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RequestMapping("/games")
public interface GameControllerIntf {

    @GetMapping("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    ResponseEntity<Game> getGame(String id);

    @GetMapping
    @Produces(MediaType.APPLICATION_JSON)
    List<Game> getGames();

    @PostMapping()
    @Consumes(MediaType.APPLICATION_JSON)
    ResponseEntity<Object> createGame(@RequestBody Game game);

    @PutMapping("/{id}")
    ResponseEntity updateGame(@PathVariable String id, @RequestBody Game game);

    @DeleteMapping("/{id}")
    ResponseEntity<Object> deleteGame(@PathVariable String id);
}
