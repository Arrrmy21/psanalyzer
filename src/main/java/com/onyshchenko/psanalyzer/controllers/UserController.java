package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.controllers.interfaces.UserControllerIntf;
import com.onyshchenko.psanalyzer.model.User;
import com.onyshchenko.psanalyzer.services.scheduler.ScheduledTasksService;
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
    private static final String USER_NOT_FOUND_EXCEPTION = "User not found.";
    @Autowired
    private UserService userService;
    @Autowired
    private ScheduledTasksService scheduledTasksService;

    @Override
    public Page<User> getUsers(int page, int size) {
        LOGGER.info("Get users request. Page: [{}], size: [{}]", page, size);
        return userService.getAllUsers(page, size);
    }

    @Override
    public Optional<User> getUser(long id) {
        LOGGER.info("Get user request. User id: [{}]", id);
        return userService.findUserById(id);
    }

    @Override
    public ResponseEntity<Object> createUser(User user) {
        LOGGER.info("Create user request. Username [{}])", user.getUsername());
        String createdUser = userService.registerUser(user);
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
        LOGGER.info("Delete user request. User id: [{}]", id);
        Optional<User> user = userService.findUserById(id);
        if (user.isPresent()) {
            userService.deleteUser(user.get());
            LOGGER.info("User [{}] successfully deleted.", id);
            return new ResponseEntity<>("User deleted.", HttpStatus.OK);
        } else {
            throw new NoSuchElementException(USER_NOT_FOUND + id);
        }
    }

    @Override
    public ResponseEntity<Object> updateUser(User userDetails) {
        LOGGER.info("Update user request. Username: [{}]", userDetails.getUsername());
        Optional<User> user = userService.updateUser(userDetails);
        if (user.isPresent()) {
            LOGGER.info("User successfully updated.");
            return new ResponseEntity<>("User updated.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User doesn't exist.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Object> addGameToWishList(long userId, long gameId) {
        LOGGER.info("Add game to wishlist request. User id: [{}]. Game Id: [{}]", userId, gameId);
        return userService.addGameToWishList(userId, gameId);
    }

    @Override
    public ResponseEntity<Object> deleteGameFromWishList(long userId, long gameId) {
        LOGGER.info("Delete game from wishlist request. User id: [{}]. Game Id: [{}]", userId, gameId);
        return userService.deleteGameFromWishList(userId, gameId);
    }

    @Override
    public ResponseEntity<Object> clearWishList(long userId) {
        LOGGER.info("Delete all games from wishlist request. User id: [{}].", userId);
        return userService.clearWishListOfUser(userId);
    }

    @Override
    public ResponseEntity<Object> notifyAllUsersProcedureStart() {
        LOGGER.info("Notify all users request.");
        scheduledTasksService.checkUsersWishListAndSendNotifications();
        return new ResponseEntity<>("Users notified.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> turnOnNotifications(long userId) {
        LOGGER.info("Turn ON notifications for user request. User id: [{}].", userId);
        return userService.turnOnUsersNotifications(userId);
    }

    @Override
    public ResponseEntity<Object> turnOffNotifications(long userId) {
        LOGGER.info("Turn OFF notifications for user request. User id: [{}].", userId);
        return userService.turnOffUsersNotifications(userId);
    }

    @Override
    public ResponseEntity<Object> getUserNotifications(long userId) {
        LOGGER.info("Get info about notifications for user request. User id: [{}].", userId);
        Optional<User> user = userService.findUserById(userId);
        if (user.isPresent()) {
            boolean userNotification = user.get().isNotification();
            return new ResponseEntity<>("User notification = [" + userNotification + "].", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(USER_NOT_FOUND_EXCEPTION, HttpStatus.OK);
        }
    }
}
