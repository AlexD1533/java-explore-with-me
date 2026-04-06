package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.*;

public record NewCategoryDto(
        @NotBlank
        @Size(min = 1, max = 50)
        String name
) {
}
