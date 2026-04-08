package ru.practicum.ewm.validation;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.exception.ValidationException;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class Validation {

    public void dataTimeValidation(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return;
        }
        if (end.isBefore(start)) {
            throw new ValidationException("Дата начала не может быть позже даты конца");
        }
    }

}

