package com.onyshchenko.psanalyzer.controllers.interfaces;

import com.onyshchenko.psanalyzer.model.User;
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

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@RequestMapping("/users")
public interface UserControllerIntf {

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Produces(MediaType.APPLICATION_JSON)
    Optional<User> getUser(@PathVariable(value = "id") long id);

    @Transactional
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Produces(MediaType.APPLICATION_JSON)
    Page<User> getUsers(@RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false, defaultValue = "10") int size);

    @PostMapping
    @Consumes(MediaType.APPLICATION_JSON)
    ResponseEntity<Object> createUser(@RequestBody User user);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Object> deleteUser(@PathVariable(value = "id") long id);

    @PutMapping
    @Consumes({MediaType.APPLICATION_JSON})
    ResponseEntity<Object> updateUser(@RequestBody User user);

    @PutMapping("/{userId}/{gameId}")
    @Consumes(MediaType.APPLICATION_JSON)
    ResponseEntity<Object> addGameToWishList(@PathVariable("userId") long userId,
                                             @PathVariable("gameId") long gameId);

    @DeleteMapping("/{userId}/{gameId}")
    ResponseEntity<Object> deleteGameFromWishList(@PathVariable(value = "userId") long userId,
                                                  @PathVariable(value = "gameId") long gameId);

    @DeleteMapping("/{userId}/all")
    ResponseEntity<Object> clearWishList(@PathVariable(value = "userId") long userId);

    @PostMapping("/notifyAll")
    @Consumes(MediaType.APPLICATION_JSON)
    ResponseEntity<Object> notifyAllUsersProcedureStart();

    @PostMapping("/{userId}/notifications/on")
    ResponseEntity<Object> turnOnNotifications(@PathVariable("userId") long userId);

    @PostMapping("/{userId}/notifications/off")
    ResponseEntity<Object> turnOffNotifications(@PathVariable("userId") long userId);

    @GetMapping("/{userId}/notifications")
    ResponseEntity<Object> getUserNotifications(@PathVariable("userId") long userId);
}
