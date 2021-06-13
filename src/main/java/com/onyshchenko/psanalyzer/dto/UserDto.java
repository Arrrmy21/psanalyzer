package com.onyshchenko.psanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.onyshchenko.psanalyzer.model.User;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserDto {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String chatId;

    public User toUser() {
        var user = new User();

        user.setUserId(id);
        user.setUsername(username);
        user.setFirstName(username);
        user.setLastName(lastName);
        user.setChatId(chatId);

        return user;
    }

    public static UserDto fromUser(User user) {
        var userDto = new UserDto();
        userDto.setId(user.getUserId());
        userDto.setUsername(user.getUsername());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setChatId(user.getChatId());

        return userDto;
    }
}
