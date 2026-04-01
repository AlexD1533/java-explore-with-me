package ru.practicum.ewm.category;

import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto toCategoryDto(Category category) {
        if (category == null) return null;
        return new CategoryDto(category.getId(), category.getName());
    }

    public Category toCategory(NewCategoryDto dto) {
        if (dto == null) return null;
        return Category.builder()
                .name(dto.name())
                .build();
    }

    public void updateCategoryFromDto(NewCategoryDto dto, Category category) {
        if (dto == null || category == null) return;
        if (dto.name() != null) {
            category.setName(dto.name());
        }
    }
}