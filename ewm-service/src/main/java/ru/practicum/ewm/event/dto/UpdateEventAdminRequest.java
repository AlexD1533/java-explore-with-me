package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.location.LocationDto;
import ru.practicum.ewm.StateActionAdmin;

public record UpdateEventAdminRequest(
        @Size(min = 20, max = 2000)
        String annotation,

        Long category,

        @Size(min = 20, max = 7000)
        String description,

        String eventDate,

        LocationDto location,

        Boolean paid,
        @PositiveOrZero
        Integer participantLimit,

        Boolean requestModeration,

        StateActionAdmin stateAction,

        @Size(min = 3, max = 120)
        String title
) {
}