package ru.practicum.ewm.controller;

import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.UserService;
import ru.practicum.ewm.validation.Validation;

@RestController
@RequestMapping(path = "/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final Validation validation;
    private final UserService userService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest request) {
        log.info("Пользователь: запрос на создание {}", request);
        validation.userEmailValidation(request.email());

        UserDto createdUser = userService.create(request);
        log.info("Пользователь создан с id={}", createdUser.id());
        return createdUser;
    }


}
