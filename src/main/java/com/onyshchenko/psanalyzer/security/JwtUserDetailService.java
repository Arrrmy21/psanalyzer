package com.onyshchenko.psanalyzer.security;

import com.onyshchenko.psanalyzer.security.jwt.JwtUserFactory;
import com.onyshchenko.psanalyzer.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUserDetailService.class);

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {

        var user = userService.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User with username [" + username + "] not found.");
        }

        var jwtUser = JwtUserFactory.create(user);

        LOGGER.info("User with username [{}] successfully loaded", user.getUsername());
        return jwtUser;
    }
}
