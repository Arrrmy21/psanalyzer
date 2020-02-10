package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.dao.UserRepository;
import com.onyshchenko.psanalyzer.interfaces.controllers.UserControllerIntf;
import com.onyshchenko.psanalyzer.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class UserController implements UserControllerIntf {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<User> getUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Optional<User> getUser(long id) {
        return userRepository.findById(id);
    }

    @Override
    public ResponseEntity<Object> createUser(User user) {
        userRepository.save(user);
        return new ResponseEntity<>("User created.", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> deleteUser(long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("User not found id: " + id));
        userRepository.delete(user);
        return new ResponseEntity<>("User deleted.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> updateUser(User userDetails) {
        userRepository.findById(userDetails.getId()).orElseThrow(
                () -> new IllegalArgumentException("User not found id: " + userDetails.getId()));

        userRepository.save(userDetails);
        return new ResponseEntity<>("User updated.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> addGameToWishList(long userId, String gameId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User not found id: " + userId));
        user.getWishList().add(gameId);
        userRepository.save(user);
        return new ResponseEntity<>("Game id: " + gameId + " added to wish list.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> deleteGameFromWishList(long userId, String gameId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User not found id: " + userId));
        user.getWishList().remove(gameId);
        userRepository.save(user);
        return new ResponseEntity<>("Game id:+" + gameId + " deleted from wish list.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> clearWishList(long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User not found id: " + userId));
        user.getWishList().clear();
        userRepository.save(user);
        return new ResponseEntity<>("Wish list cleaned up.", HttpStatus.OK);
    }
}
