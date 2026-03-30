package ru.practicum.ewm.compilation;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.event.EventShortDto;

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