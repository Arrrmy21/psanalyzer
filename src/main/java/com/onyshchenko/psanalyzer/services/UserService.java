package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.dao.RoleRepository;
import com.onyshchenko.psanalyzer.dao.UserRepository;
import com.onyshchenko.psanalyzer.model.Role;
import com.onyshchenko.psanalyzer.model.Status;
import com.onyshchenko.psanalyzer.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private static final String USER_NOT_EXIST = "User with id: %s doesn't exist.";
    private static final String GAME_NOT_EXIST = "Game with id: %s doesn't exist.";

    @Value("${default.user.password}")
    private String defaultPassword;

    @Autowired
    private GameService gameService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String registerUser(User user) {

        LOGGER.info("Trying to register user with username [{}].", user.getUsername());
        if (user.getUserId() != 0) {
            Optional<User> userFromDb = userRepository.findById(user.getUserId());
            if (userFromDb.isPresent()) {
                LOGGER.info("User id is present in database.");
                return null;
            }
        } else if (user.getUsername() != null) {
            Optional<User> userFromDb = Optional.ofNullable(userRepository.findByUsername(user.getUsername()));
            if (userFromDb.isPresent()) {
                return null;
            }
        } else {
            return "User needs userName for registration.";
        }

        user.setUserId(user.getUserId());

        Role roleUser = roleRepository.findByName("ROLE_USER");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(roleUser);
        user.setRoles(userRoles);

        user.setStatus(Status.ACTIVE);

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode(defaultPassword));
        }

        User registeredUser = userRepository.save(user);

        LOGGER.info("User [{}] successfully registered.", registeredUser.getUsername());

        return "User created.";
    }

    public Page<User> getAllUsers(int page, int size) {

        Page<User> users = userRepository.findAll(PageRequest.of(page, size));

        LOGGER.info("Getting {} users.", users.getTotalElements());

        return users;
    }

    public Optional<User> findUserById(long id) {

        Optional<User> result = userRepository.findById(id);

        result.ifPresent(user -> LOGGER.info("Getting user by id {}.", user.getUserId()));

        return result;
    }

    public User findByUsername(String username) {

        User result = userRepository.findByUsername(username);

        LOGGER.info("Getting user by name {}.", result.getUsername());

        return result;
    }

    public List<Long> getAllUsersWithDiscountOnGameInWishlist() {
        return userRepository.getIdOfUsersThatHaveDiscountOnGameInWishlist();
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public Optional<User> updateUser(User userDetails) {
        Optional<User> userFromDb = findUserById(userDetails.getUserId());
        if (!userFromDb.isPresent()) {
            return Optional.empty();
        }
        User userToBeUpdated = userFromDb.get();
        userToBeUpdated.setFirstName(userDetails.getFirstName());
        userToBeUpdated.setLastName(userDetails.getLastName());
        userToBeUpdated.setUsername(userDetails.getUsername());
        userToBeUpdated.setStatus(userDetails.getStatus());
        userToBeUpdated.setPassword(userDetails.getPassword());
        userToBeUpdated.setNotification(userDetails.isNotification());
        userToBeUpdated.setChatId(userDetails.getChatId());

        return Optional.of(userRepository.save(userToBeUpdated));
    }

    public ResponseEntity<Object> addGameToWishList(long userId, long gameId) {

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(String.format(USER_NOT_EXIST, userId), HttpStatus.BAD_REQUEST);
        }
        Optional<Game> game = gameService.getGame(gameId);
        if (!game.isPresent()) {
            return new ResponseEntity<>(String.format(GAME_NOT_EXIST, gameId), HttpStatus.BAD_REQUEST);
        }

        user.get().getWishList().add(gameId);
        saveUser(user.get());
        LOGGER.info("Game id: [{}] added to wishlist of user [{}].", gameId, userId);

        return new ResponseEntity<>("Game : [" + game.get().getName() + "] added to wish list.", HttpStatus.OK);
    }

    public Optional<User> saveUser(User user) {
        User savedUser = userRepository.save(user);
        return Optional.of(savedUser);
    }

    public ResponseEntity<Object> deleteGameFromWishList(long userId, long gameId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(String.format(USER_NOT_EXIST, userId), HttpStatus.BAD_REQUEST);
        }
        Optional<Game> game = gameService.getGame(gameId);
        if (!game.isPresent()) {
            return new ResponseEntity<>(String.format(GAME_NOT_EXIST, gameId), HttpStatus.BAD_REQUEST);
        }

        user.get().getWishList().remove(gameId);
        saveUser(user.get());
        LOGGER.info("Game id: [{}] removed from wishlist of user [{}].", gameId, userId);

        return new ResponseEntity<>("Game : [" + game.get().getName() + "] removed from wish list.", HttpStatus.OK);
    }

    public ResponseEntity<Object> clearWishListOfUser(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(String.format(USER_NOT_EXIST, userId), HttpStatus.BAD_REQUEST);
        }
        user.get().getWishList().clear();
        saveUser(user.get());
        LOGGER.info("All games have been removed from wish list of user [{}].", userId);

        return new ResponseEntity<>("All games have been removed from wish list.", HttpStatus.OK);
    }

    public ResponseEntity<Object> turnOnUsersNotifications(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(String.format(USER_NOT_EXIST, userId), HttpStatus.BAD_REQUEST);
        }

        user.get().setNotification(true);
        saveUser(user.get());

        LOGGER.info("Turning ON notifications for user [{}].", user.get().getUsername());
        return new ResponseEntity<>("User notification = [true].", HttpStatus.OK);
    }

    public ResponseEntity<Object> turnOffUsersNotifications(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(String.format(USER_NOT_EXIST, userId), HttpStatus.BAD_REQUEST);
        }

        user.get().setNotification(false);
        saveUser(user.get());

        LOGGER.info("Turning OFF notifications for user [{}].", user.get().getUsername());
        return new ResponseEntity<>("User notification = [false].", HttpStatus.OK);
    }
}
