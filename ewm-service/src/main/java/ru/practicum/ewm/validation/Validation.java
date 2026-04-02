package ru.practicum.ewm.validation;

import jakarta.validation.constraints.NotNull;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventService;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.DuplicatedDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.practicipation.ParticipationRepository;
import ru.practicum.ewm.practicipation.ParticipationService;
import ru.practicum.ewm.user.StateActionAdmin;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class Validation {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

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


    public void dublicateRequests(Long userId, Event event) {

        if (!participationRepository.findAllByEventIdAndRequesterId(userId, event.getId()).isEmpty()) {
            throw new ConflictException(
                    "Пользователь с id " + userId + "уже оставил заявку на участие в событие с id " + event.getId());
        }
    }

    public void currentUserValidation(Long userId, Event event) {

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(
                    "Текущий пользователь при создании заявки на участие не может быть инициатором события");
        }
    }

    public void noPublicEventValidation(Event event) {

        if (event.getState().equals(EventState.CANCELED) || event.getState().equals(EventState.PENDING)) {
            throw new ConflictException(
                    "Нельзя участвовать в неопубликованном событии");
        }
    }

    public void publicEventValidation(Event event) {

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(
                    "Изменить можно только отмененные события или события в состоянии ожидания модерации");
        }
    }

    public void limitRequestsValidation(Event event) {

        Integer requestCount = participationRepository.countByEventId(event.getId());

        if (event.getParticipantLimit() <= requestCount) {
            throw new ConflictException(
                    "У события достигнут лимит запросов на участие");
        }
    }

    public void validateEventDateAdminUpdate(String s, LocalDateTime publishedOnTime) {
        if (s == null) {
            return;
        }

        LocalDateTime eventDate = LocalDateTime.parse(s, FORMATTER);
        LocalDateTime minDate = publishedOnTime.plusHours(1);


        if (eventDate.isBefore(minDate)) {
            throw new ValidationException(
                    "Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"
            );
        }
    }


    public void publicEventValidation(UpdateEventAdminRequest request, Event event) {

        if (event.getState().equals(EventState.PUBLISHED) && request.stateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
            throw new ConflictException("Cобытие можно публиковать, только если оно в состоянии ожидания публикации");
        }
        if (event.getState().equals(EventState.CANCELED) && request.stateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
            throw new ConflictException("Cобытие можно публиковать, только если оно в состоянии ожидания публикации");
        }

        if (event.getState().equals(EventState.CANCELED) && request.stateAction().equals(StateActionAdmin.REJECT_EVENT)) {
            throw new ConflictException("событие можно отклонить, только если оно еще не опубликовано");
        }
         if (event.getState().equals(EventState.PUBLISHED) && request.stateAction().equals(StateActionAdmin.REJECT_EVENT)) {
            throw new ConflictException("событие можно отклонить, только если оно еще не опубликовано");
        }
    }


}
