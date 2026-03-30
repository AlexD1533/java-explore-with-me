package ru.practicum.ewm.user;
import jakarta.validation.constraints.*;

public record UserDto(
        Long id,

        @NotBlank
        @Size(min = 2, max = 250)
        String name,

        @NotBlank
        @Email
        @Size(min = 6, max = 254)
        String email
) {}
