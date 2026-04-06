package ru.practicum.ewm.category;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@Slf4j
@Validated
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;


    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@Positive @NotNull @PathVariable Long id) {
        log.info("Категория: запрос на получение по id={}", id);

        CategoryDto category = categoryService.getById(id);
        log.info("Найден  пользователь: {}", category);
        return category;
    }


    @GetMapping
    public List<CategoryDto> getCategoryAllByFilter(@RequestParam(defaultValue = "0") Integer from,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        log.info("Категории: запрос на получение");

        List<CategoryDto> categories = categoryService.getCategoryAllByFilter(from, size);
        log.info("Список категорий {}", categories);
        return categories;
    }


}
