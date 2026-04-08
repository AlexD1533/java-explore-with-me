package ru.practicum.ewm.validation;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.exception.ValidationException;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
@RequiredArgsConstructor
public class Validation {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void dataTimeValidation(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return;
        }
        if (end.isBefore(start)) {
            throw new ValidationException("Дата начала не может быть позже даты конца");
        }
    }

}

