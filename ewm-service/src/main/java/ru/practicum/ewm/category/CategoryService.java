package ru.practicum.ewm.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
