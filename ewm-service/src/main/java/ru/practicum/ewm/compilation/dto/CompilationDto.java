package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.Set;

public record CompilationDto(
        @NotNull
        Long id,

        @NotNull
        Boolean pinned,

        @NotBlank
        String title,

        Set<EventShortDto> events
) {
}