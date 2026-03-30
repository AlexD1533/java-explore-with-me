package ru.practicum.ewm.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record CompilationDto(
        @NotNull
        Long id,

        @NotNull
        Boolean pinned,

        @NotBlank
        String title,

        Set<EventShortDto> events
) {}