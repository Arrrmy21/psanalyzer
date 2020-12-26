package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.dao.UserRepository;
import com.onyshchenko.psanalyzer.controllers.interfaces.UserControllerIntf;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.User;
import com.onyshchenko.psanalyzer.services.GameService;
import com.onyshchenko.psanalyzer.services.HtmlHookService;
import com.onyshchenko.psanalyzer.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class UserController implements UserControllerIntf {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private static final String USER_NOT_FOUND = "User not found id: ";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private GameService gameService;
    @Autowired
    private HtmlHookService htmlHookService;

    @Override
    public Page<User> getUsers(int page, int size) {
        return userService.findAll(page, size);
    }

    @Override
    public Optional<User> getUser(long id) {
        return userService.findById(id);
    }

    @Override
    public ResponseEntity<Object> createUser(User user) {

        String createdUser = userService.register(user);
        if (createdUser == null) {
            return new ResponseEntity<>("User already exists.", HttpStatus.OK);
        } else if (createdUser.equalsIgnoreCase("User needs userName for registration.")) {
            return new ResponseEntity<>("Sorry, but service available only for users with userName. Please provide it in settings", HttpStatus.OK);
        } else if (createdUser.equalsIgnoreCase("User created.")) {
            return new ResponseEntity<>("User created.", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("Unknown operation result.", HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<Object> deleteUser(long id) {

        User user = userRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException(USER_NOT_FOUND + id));
        userRepository.delete(user);
        return new ResponseEntity<>("User deleted.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> updateUser(User userDetails) {
        userRepository.findById(userDetails.getUserId()).orElseThrow(
                () -> new IllegalArgumentException(USER_NOT_FOUND + userDetails.getUserId()));

        userRepository.save(userDetails);
        return new ResponseEntity<>("User updated.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> addGameToWishList(long userId, String gameId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException(USER_NOT_FOUND + userId));
        Optional<Game> game = gameService.findGameIfAlreadyExists(gameId);
        if (game.isPresent()) {
            user.getWishList().add(gameId);
            userRepository.save(user);
            LOGGER.info("Game id: [{}] added to wishlist of user [{}].", gameId, userId);
            return new ResponseEntity<>("Game : [" + game.get().getName() + "] added to wish list.", HttpStatus.OK);
        }
        return new ResponseEntity<>("Game with id: " + gameId + " doesn't exist.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> deleteGameFromWishList(long userId, String gameId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException(USER_NOT_FOUND + userId));
        Optional<Game> game = gameService.findGameIfAlreadyExists(gameId);
        if (game.isPresent()) {
            user.getWishList().remove(gameId);
            userRepository.save(user);
            LOGGER.info("Game id: [{}] removed from wishlist of user [{}].", gameId, userId);
            return new ResponseEntity<>("Game : [" + game.get().getName() + "] removed from  wish list.", HttpStatus.OK);
        }
        return new ResponseEntity<>("Game with id: " + gameId + " doesn't exist.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> clearWishList(long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException(USER_NOT_FOUND + userId));
        user.getWishList().clear();
        userRepository.save(user);
        return new ResponseEntity<>("Wish list cleaned up.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> notifyAllUsersProcedureStart() {
        htmlHookService.checkUsersWishListAndSendNotifications();
        return new ResponseEntity<>("Users notified.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> turnOnNotifications(long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            LOGGER.info("Turning OFF notifications for user [{}].", user.get().getUsername());
            user.get().setNotification(true);
            userRepository.save(user.get());
            return new ResponseEntity<>("User notification = true.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found.", HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Object> turnOffNotifications(long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            LOGGER.info("Turning ON notifications for user [{}].", user.get().getUsername());
            user.get().setNotification(false);
            userRepository.save(user.get());
            return new ResponseEntity<>("User notification = false.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found.", HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Object> getUserNotifications(long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            boolean userNotification = user.get().isNotification();
            return new ResponseEntity<>("User notification = [" + userNotification + "].", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found.", HttpStatus.OK);
        }
    }
}
