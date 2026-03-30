package ru.practicum.ewm.event;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.location.LocationDto;
import ru.practicum.ewm.user.StateActionAdmin;

public record UpdateEventAdminRequest(
        @Size(min = 20, max = 2000)
        String annotation,

        Long category,

        @Size(min = 20, max = 7000)
        String description,

        String eventDate,

        LocationDto locationDto,

        Boolean paid,

        Integer participantLimit,

        Boolean requestModeration,

        StateActionAdmin stateAction,

        @Size(min = 3, max = 120)
        String title
) {}