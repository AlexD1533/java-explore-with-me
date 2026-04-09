package ru.practicum.ewm.user.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.user.User;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public UserShortDto toUserShortDto(User user) {
        if (user == null) return null;
        return new UserShortDto(user.getId(), user.getName());
    }

    public User toUser(NewUserRequest dto) {
        if (dto == null) return null;
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .build();
    }
}