package ru.practicum.ewm.dto;
import jakarta.validation.constraints.*;

public record UserShortDto(
        @NotNull
        Long id,

        @NotBlank
        @Size(min = 2, max = 250)
        String name
) {}
