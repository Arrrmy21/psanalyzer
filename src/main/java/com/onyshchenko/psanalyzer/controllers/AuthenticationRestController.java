package com.onyshchenko.psanalyzer.controllers;

import com.onyshchenko.psanalyzer.dto.AuthenticationRequestDto;
import com.onyshchenko.psanalyzer.model.User;
import com.onyshchenko.psanalyzer.security.jwt.JwtTokenProvider;
import com.onyshchenko.psanalyzer.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/auth")
public class AuthenticationRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationRestController.class);

    @Value("${default.user.password}")
    private String defaultPassword;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody AuthenticationRequestDto requestDto) {
        try {
            LOGGER.info("Starting authentication of user [{}].", requestDto.getUsername());
            String username = requestDto.getUsername();

            String password = defaultPassword;
            if (requestDto.getPassword() != null) {
                password = requestDto.getPassword();
            }
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,
                    password));
            User user = userService.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("User with username [" + username + "] not found.");
            }

            String token = jwtTokenProvider.createToken(username, user.getRoles());

            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            response.put("token", token);

            return ResponseEntity.of(Optional.of(response));

        } catch (AuthenticationException ex) {
            LOGGER.info("Invalid username or password");
            return new ResponseEntity<>("Invalid username or password", HttpStatus.BAD_REQUEST);
        }
    }
}
