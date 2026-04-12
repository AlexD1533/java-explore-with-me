package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.location.LocationDto;
import ru.practicum.ewm.user.dto.UserShortDto;

public record EventFullDto(
        @NotBlank
        String annotation,

        CategoryDto category,

        Long confirmedRequests,

        String createdOn,

        String description,

        String eventDate,

        Long id,

        UserShortDto initiator,

        LocationDto location,

        Boolean paid,

        Integer participantLimit,

        String publishedOn,

        Boolean requestModeration,

        EventState state,

        String title,

        Long views
) {
}
