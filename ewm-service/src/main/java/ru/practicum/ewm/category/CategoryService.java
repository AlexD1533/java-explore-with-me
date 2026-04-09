package ru.practicum.ewm.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategoryMapper;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;


import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    public CategoryDto create(NewCategoryDto request) {

        Category newCategory = categoryRepository.save(categoryMapper.toCategory(request));
        return categoryMapper.toCategoryDto(newCategory);
    }

    public CategoryDto getById(Long id) {

        Category newCategory = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id=" + id + " не найдена"));

        return categoryMapper.toCategoryDto(newCategory);
    }

    public List<CategoryDto> getCategoryAllByFilter(Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        return categories.stream().map(categoryMapper::toCategoryDto).toList();
    }

    public void delete(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Категория с id=" + categoryId + " не найдена"));

        Long countEvents = eventRepository.countByCategoryName(category.getName());

        if (countEvents != 0) {
            throw new ConflictException("Категория имеет связанные события. Удаление невозможно");
        }
        categoryRepository.deleteById(categoryId);
    }

    public CategoryDto update(Long id, NewCategoryDto request) {

        Category category = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id=" + id + " не найдена"));

        categoryMapper.updateCategoryFromDto(request, category);
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }
}

