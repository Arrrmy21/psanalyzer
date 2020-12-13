package com.onyshchenko.psanalyzer.services;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    @Value("${default.user.password}")
    private static final String DEFAULT_PASSWORD = "defaultPassword";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String register(User user) {

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
        user.setStatus(Status.ACTIVE);
        user.setRoles(userRoles);

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        }

        User registeredUser = userRepository.save(user);

        LOGGER.info("User [{}] successfully registered.", registeredUser.getUsername());

        return "User created.";
    }

    public Page<User> findAll(int page, int size) {

        Page<User> users = userRepository.findAll(PageRequest.of(page, size));

        LOGGER.info("Getting {} users.", users.getTotalElements());

        return users;
    }

    public Optional<User> findById(long id) {

        Optional<User> result = userRepository.findById(id);

        result.ifPresent(user -> LOGGER.info("Getting user by id {}.", user.getUserId()));

        return result;
    }

    public User findByUsername(String username) {

        User result = userRepository.findByUsername(username);

        LOGGER.info("Getting user by name {}.", result.getUsername());

        return result;
    }

    public ArrayList<Long> getAllUsersWithDiscountOnGameInWishlist() {

        return userRepository.getIdOfUsersThatHaveDiscountOnGameInWishlist();
    }
}
