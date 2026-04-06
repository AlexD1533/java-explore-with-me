package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.*;

import java.util.Set;

public record UpdateCompilationRequest(
        Set<Long> events,

        Boolean pinned,

        @Size(min = 1, max = 50)
        String title
) {

    public UpdateCompilationRequest {
        if (events == null) {
            events = Set.of();
        }
    }
}
