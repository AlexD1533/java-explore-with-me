package ru.practicum.ewm.validation;

import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.DuplicatedDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class Validation {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int HOURS_BEFORE_EVENT = 2;

    public void userEmailValidation(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicatedDataException("Email " + email + " уже используется");
        }
    }


    public void userIdValidation(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new DuplicatedDataException("userId " + userId + " не существует");
        }
    }

    public void categoryNameValidation(String name) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new DuplicatedDataException("Name " + name + " уже используется");
        }
    }

    public void categoryIdValidation(Long id) {
        if (categoryRepository.findById(id).isPresent()) {
            throw new DuplicatedDataException("Id " + id + " уже используется");
        }
    }


    public void validateEventDate(String eventDateStr) {
        if (eventDateStr == null) {
            return;
        }
        LocalDateTime eventDate = LocalDateTime.parse(eventDateStr, FORMATTER);
        LocalDateTime minDate = LocalDateTime.now().plusHours(HOURS_BEFORE_EVENT);


        if (eventDate.isBefore(minDate)) {
            throw new ValidationException(
                    "Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"
            );
        }
    }

    public void eventIdValidation(Long eventId) {
        if (eventRepository.findById(eventId).isEmpty()) {
            throw new DuplicatedDataException("eventId " + eventId + " не существует");
        }
    }
}
