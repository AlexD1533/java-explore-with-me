package ru.practicum.ewm.admin;

import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.CategoryService;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.CompilationService;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.event.EventService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.UserService;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.validation.Validation;

import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@Slf4j
@Validated
@RequiredArgsConstructor
public class AdminController {

    private final Validation validation;
    private final UserService userService;
    private final CategoryService categoryService;
    private final AdminService adminService;
    private final EventService eventService;
    private final CompilationService compilationService;

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
            @RequestParam(required = false) List<Integer> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size

    ) {
        System.out.println(ids + " " + from + " " + size);
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

        CategoryDto createdCategory = categoryService.create(request);
        log.info("Пользователь создан с id={}", createdCategory.id());
        return createdCategory;
    }


    @PatchMapping("/categories/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody NewCategoryDto request) {
        log.info("Категория: запрос на обновление {}", request);
        validation.categoryNameUpdateValidation(request.name(), categoryId);
        CategoryDto updatedCategory = categoryService.update(categoryId, request);
        log.info("Категория обновлена с id={}", updatedCategory.id());
        return updatedCategory;
    }


    @PatchMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest request) {
        log.info("Событие: запрос на обновление {}", request);

        validation.timeFutureValidation(request.eventDate());

        EventFullDto updatedEvent = adminService.updateEventAdmin(eventId, request);
        log.info("Событие обновлено с id={}", updatedEvent.id());
        return updatedEvent;
    }


    @GetMapping("/events")
    public List<EventFullDto> getEventsByParamAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size

    ) {
        log.info("События: запрос на получение информации");

        List<EventFullDto> events = eventService.searchEventsInfoByParm(users, states, categories, rangeStart, rangeEnd, from, size);
        validation.dataTimeValidation(rangeStart, rangeEnd);
        log.info("Результат поиска: {}", events);
        return events;
    }


    @DeleteMapping("categories/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        log.info("Категория: запрос на удаление {}", categoryId);
        categoryService.delete(categoryId);
        log.info("Категория {} удалена", categoryId);

    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto request) {
        log.info("Подборка: запрос на создание {}", request);
        CompilationDto createdCompilation = compilationService.create(request);
        log.info("Подборка создана с id={}", createdCompilation.id());
        return createdCompilation;
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Подборка: запрос на удаление {}", compId);
        compilationService.delete(compId);
        log.info("Категория {} удалена", compId);

    }

    @PatchMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(
            @PathVariable Long compId,
            @Valid @RequestBody UpdateCompilationRequest request) {
        log.info("Подборка: запрос на обновление {}", compId);

        CompilationDto updatedCompilation = compilationService.updateCompilation(compId, request);
        log.info("Подборка обновлена с id={}", updatedCompilation.id());
        return updatedCompilation;
    }
}
