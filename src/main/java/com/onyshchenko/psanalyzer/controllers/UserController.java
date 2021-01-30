package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.controllers.interfaces.UserControllerIntf;
import com.onyshchenko.psanalyzer.model.User;
import com.onyshchenko.psanalyzer.services.ScheduledTasksService;
import com.onyshchenko.psanalyzer.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class UserController implements UserControllerIntf {

    private static final String USER_NOT_FOUND = "User not found id: ";
    private static final String USER_NOT_FOUND_EXCEPTION = "User not found.";
    @Autowired
    private UserService userService;
    @Autowired
    private ScheduledTasksService scheduledTasksService;

    @Override
    public Page<User> getUsers(int page, int size) {
        return userService.getAllUsers(page, size);
    }

    @Override
    public Optional<User> getUser(long id) {
        return userService.findUserById(id);
    }

    @Override
    public ResponseEntity<Object> createUser(User user) {
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
        Optional<User> user = userService.findUserById(id);
        if (user.isPresent()) {
            userService.deleteUser(user.get());
            return new ResponseEntity<>("User deleted.", HttpStatus.OK);
        } else {
            throw new NoSuchElementException(USER_NOT_FOUND + id);
        }
    }

    @Override
    public ResponseEntity<Object> updateUser(User userDetails) {
        Optional<User> user = userService.updateUser(userDetails);
        if (user.isPresent()) {
            return new ResponseEntity<>("User updated.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User doesn't exist.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Object> addGameToWishList(long userId, long gameId) {
        return userService.addGameToWishList(userId, gameId);
    }

    @Override
    public ResponseEntity<Object> deleteGameFromWishList(long userId, long gameId) {
        return userService.deleteGameFromWishList(userId, gameId);
    }

    @Override
    public ResponseEntity<Object> clearWishList(long userId) {
        return userService.clearWishListOfUser(userId);
    }

    @Override
    public ResponseEntity<Object> notifyAllUsersProcedureStart() {
        scheduledTasksService.checkUsersWishListAndSendNotifications();
        return new ResponseEntity<>("Users notified.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> turnOnNotifications(long userId) {
        return userService.turnOnUsersNotifications(userId);
    }

    @Override
    public ResponseEntity<Object> turnOffNotifications(long userId) {
        return userService.turnOffUsersNotifications(userId);
    }

    @Override
    public ResponseEntity<Object> getUserNotifications(long userId) {
        Optional<User> user = userService.findUserById(userId);
        if (user.isPresent()) {
            boolean userNotification = user.get().isNotification();
            return new ResponseEntity<>("User notification = [" + userNotification + "].", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(USER_NOT_FOUND_EXCEPTION, HttpStatus.OK);
        }
    }
}
