package ru.practicum.ewm.dto;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.model.enums.StateActionUser;

public record UpdateEventUserRequest(
        @Size(min = 20, max = 2000)
        String annotation,

        Long category,

        @Size(min = 20, max = 7000)
        String description,

        String eventDate,

        Location location,

        Boolean paid,

        Integer participantLimit,

        Boolean requestModeration,

        StateActionUser stateAction,

        @Size(min = 3, max = 120)
        String title
) {}