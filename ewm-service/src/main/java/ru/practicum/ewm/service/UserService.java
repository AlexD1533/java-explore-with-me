package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import lombok.RequiredArgsConstructor;

import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto create(NewUserRequest request) {

        User newUser = userRepository.save(userMapper.toUser(request));
        return userMapper.toUserDto(newUser);
    }
}
