package ru.practicum.ewm.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
private final CategoryMapper categoryMapper;

    public CategoryDto create(NewCategoryDto request) {

        Category newCategory = categoryRepository.save(categoryMapper.toCategory(request));
        return categoryMapper.toCategoryDto(newCategory);
    }
}
