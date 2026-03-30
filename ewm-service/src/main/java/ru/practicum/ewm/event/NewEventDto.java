package ru.practicum.ewm.event;

import jakarta.validation.constraints.*;
import ru.practicum.ewm.location.LocationDto;

public record NewEventDto(
        @NotBlank
        @Size(min = 20, max = 2000)
        String annotation,

        @NotNull
        Long category,

        @NotBlank
        @Size(min = 20, max = 7000)
        String description,

        @NotBlank
        String eventDate,

        @NotNull
        LocationDto locationDto,

        Boolean paid,

        Integer participantLimit,

        Boolean requestModeration,

        @NotBlank
        @Size(min = 3, max = 120)
        String title
) {}
