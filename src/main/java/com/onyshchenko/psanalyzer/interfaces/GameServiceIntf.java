package com.onyshchenko.psanalyzer.interfaces;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@RequestMapping("/games")
public interface GameServiceIntf {

    @GetMapping("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Game getGame(String id);

    @GetMapping
    @Produces(MediaType.APPLICATION_JSON)
    Collection<Game> getGames();

    @PostMapping()
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ResponseEntity<Object> createGame(@RequestBody Game game);

    @PutMapping("/{id}")
    ResponseEntity<Object> updateGame(@PathVariable String id, @RequestBody Game game);

    @DeleteMapping("/{id}")
    ResponseEntity<Object> deleteGame(@PathVariable String id);



}
