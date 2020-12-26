package com.onyshchenko.psanalyzer.controllers.interfaces;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.ValidationException;
import java.util.Optional;

@RequestMapping("/games")
public interface GameControllerIntf {

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Produces(MediaType.APPLICATION_JSON)
    Optional<Game> getGame(String id);

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Produces(MediaType.APPLICATION_JSON)
    Page<Game> getGames(@RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, name = "filter") String filter) throws ValidationException;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Consumes(MediaType.APPLICATION_JSON)
    ResponseEntity<Object> createGame(@RequestBody Game game);

    @PutMapping()
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity updateGame(@RequestBody Game game);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Object> deleteGame(@PathVariable String id);

    @PostMapping("/startUpdateProcedure")
    @PreAuthorize("hasRole('ADMIN')")
    @Consumes(MediaType.APPLICATION_JSON)
    ResponseEntity<Object> startUpdateGameProcedure();
}
