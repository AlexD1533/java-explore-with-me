package dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        List<String> errors,
        String message,
        String reason,
        HttpStatus status,
        LocalDateTime timestamp
) {}