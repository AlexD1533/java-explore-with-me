package ru.practicum.ewm.compilation;

import jakarta.validation.constraints.*;

import java.util.Set;

public record NewCompilationDto(
        Set<Long> events,

        Boolean pinned,

        @NotBlank
        @Size(min = 1, max = 50)
        String title
) {
    public NewCompilationDto {
        if (events == null) {
            events = Set.of();
        }
    }
}
