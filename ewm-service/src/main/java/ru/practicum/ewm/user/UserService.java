package ru.practicum.ewm.user;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto create(NewUserRequest request) {

        User newUser = userRepository.save(userMapper.toUser(request));
        return userMapper.toUserDto(newUser);
    }

    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<UserDto> searchUsersInfo(List<Integer> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        if (ids.isEmpty()) {
            List<User> users = userRepository.findAll(pageable).getContent();
            return users.stream()
                    .map(userMapper::toUserDto)
                    .toList();
        }
        return userRepository.findAllByIdIn(ids, pageable);
    }
}

