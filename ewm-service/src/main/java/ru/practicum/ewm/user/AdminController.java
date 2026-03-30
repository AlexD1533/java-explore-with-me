package ru.practicum.ewm.user;

import ru.practicum.ewm.category.CategoryDto;
import ru.practicum.ewm.category.NewCategoryDto;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.CategoryService;
import ru.practicum.ewm.validation.Validation;

import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final Validation validation;
    private final UserService userService;
    private final CategoryService categoryService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest request) {
        log.info("Пользователь: запрос на создание {}", request);
        validation.userEmailValidation(request.email());

        UserDto createdUser = userService.create(request);
        log.info("Пользователь создан с id={}", createdUser.id());
        return createdUser;
    }

    @DeleteMapping("users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Пользователь: запрос на удаление {}", userId);
        userService.delete(userId);
        log.info("Пользователь {} удален", userId);

    }

    @GetMapping("/users")
    public List<UserDto> getById(
            @RequestParam (required = false) List<Integer> ids,
            @RequestParam(defaultValue = "0") Integer from, // По умолчанию 0
            @RequestParam(defaultValue = "10") Integer size

    ) {
        log.info("Пользователь: запрос на получение информации");

        List<UserDto> users = userService.searchUsersInfo(ids, from, size);
        log.info("Результат поиска: {}", users);
        return users;
    }


    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto request) {
        log.info("Категория: запрос на создание {}", request);
        validation.categoryNameValidation(request.name());

        CategoryDto createdUser = categoryService.create(request);
        log.info("Пользователь создан с id={}", createdUser.id());
        return createdUser;
    }


}
