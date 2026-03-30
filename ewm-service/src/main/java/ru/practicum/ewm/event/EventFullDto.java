package ru.practicum.ewm.event;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.category.CategoryDto;
import ru.practicum.ewm.location.LocationDto;
import ru.practicum.ewm.user.UserShortDto;

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
        LocationDto locationDto,

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
