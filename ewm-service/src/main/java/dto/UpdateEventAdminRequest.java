package dto;

import jakarta.validation.constraints.*;
import model.enums.StateActionAdmin;

public record UpdateEventAdminRequest(
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

        StateActionAdmin stateAction,

        @Size(min = 3, max = 120)
        String title
) {}