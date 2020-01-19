package com.onyshchenko.psanalyzer.interfaces.controllers;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@RequestMapping("/games")
public interface GameControllerIntf {

    @GetMapping("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Optional<Game> getGame(String id);

    @GetMapping
    @Produces(MediaType.APPLICATION_JSON)
    Page<Game> getGames(@RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false, defaultValue = "10") int size);

    @PostMapping()
    @Consumes(MediaType.APPLICATION_JSON)
    ResponseEntity<Object> createGame(@RequestBody Game game);

    @PutMapping()
    ResponseEntity updateGame(@RequestBody Game game);

    @DeleteMapping("/{id}")
    ResponseEntity<Object> deleteGame(@PathVariable String id);
}
