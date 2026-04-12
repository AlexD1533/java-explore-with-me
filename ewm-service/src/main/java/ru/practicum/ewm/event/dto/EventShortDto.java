package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.dto.UserShortDto;

public record EventShortDto(
        @NotBlank
        String annotation,

        @NotNull
        CategoryDto category,

        Long confirmedRequests,

        @NotBlank
        String eventDate,

        Long id,

        @NotNull
        UserShortDto initiator,

        @NotNull
        Boolean paid,

        @NotBlank
        String title,

        Long views
) {
}
