package ru.practicum.ewm.dto;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.model.enums.EventState;

public record EventFullDto(
        @NotBlank
        String annotation,

        @NotNull
        CategoryDto category,

        Long confirmedRequests,

        String createdOn,

        String description,

        @NotBlank
        String eventDate,

        Long id,

        @NotNull
        UserShortDto initiator,

        @NotNull
        Location location,

        @NotNull
        Boolean paid,

        Integer participantLimit,

        String publishedOn,

        Boolean requestModeration,

        EventState state,

        @NotBlank
        String title,

        Long views
) {}
