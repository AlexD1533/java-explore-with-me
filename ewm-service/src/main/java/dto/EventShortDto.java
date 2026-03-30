package dto;

import jakarta.validation.constraints.*;

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
) {}
