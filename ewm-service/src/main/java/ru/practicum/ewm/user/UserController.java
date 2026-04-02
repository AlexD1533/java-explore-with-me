package ru.practicum.ewm.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.ewm.event.EventService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.practicipation.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.practicipation.EventRequestStatusUpdateResult;
import ru.practicum.ewm.practicipation.ParticipationRequestDto;
import ru.practicum.ewm.practicipation.ParticipationService;
import ru.practicum.ewm.validation.Validation;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@Validated
@RequiredArgsConstructor
public class UserController {

    private final Validation validation;
    private final EventService eventService;
    private final ParticipationService participationService;


    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@Valid @RequestBody NewEventDto request,
                                    @NotNull @PathVariable Long userId) {
        log.info("Событие: запрос на создание {}", request);
        validation.userIdValidation(userId);
        validation.validateEventDate(request.eventDate());

        EventFullDto createdEvent = eventService.create(userId, request);
        log.info("Событие создано с id={}", createdEvent.id());
        return createdEvent;
    }


    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequestEvent(
            @NotNull @PathVariable Long userId,
            @NotNull @Positive @RequestParam Long eventId) {
        log.info("Запрос: запрос на участие");

        validation.userIdValidation(userId);


        ParticipationRequestDto createdRequest = participationService.createRequestEvent(userId, eventId);

        System.out.println("ccc " + createdRequest);
        log.info("Запрос создан с id={}", createdRequest.id());
        return createdRequest;
    }


    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEventByIdByUserId(
            @NotNull @PathVariable Long userId,
            @NotNull @PathVariable Long eventId

    ) {
        log.info("Событие: запрос на получение информации");
        validation.userIdValidation(userId);

        EventFullDto event = eventService.getEventByIdByUserId(userId, eventId);
        log.info("Результат поиска: {}", event);
        return event;
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventUser(
            @NotNull @PathVariable Long eventId,
            @NotNull @PathVariable Long userId,
            @Valid @RequestBody UpdateEventUserRequest request) {
        log.info("Событие: запрос на обновление {}", request);

        validation.userIdValidation(userId);
        validation.validateEventDate(request.eventDate());


        EventFullDto updatedEvent = eventService.updateEvent(eventId, userId, request);
        log.info("Событие обновлено с id={}", updatedEvent.id());
        return updatedEvent;
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getEventsById(
            @NotNull @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Событие: запрос на получение списка событий");
        validation.userIdValidation(userId);

        List<EventShortDto> events = eventService.searchEventsByUserId(userId, from, size);
        log.info("Результат поиска: {}", events);
        return events;
    }


    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getParticipationIdByUserIdAndEventId(
            @NotNull @PathVariable Long userId,
            @NotNull @PathVariable Long eventId

    ) {
        log.info("Запрос на участие: запрос на получение информации");

        validation.userIdValidation(userId);
        validation.eventIdValidation(eventId);


        List<ParticipationRequestDto> participations = participationService.getParticipationByUserIdAndEventId(userId, eventId);
        log.info("Запросы на участие с id:{}", participations);
        return participations;
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getParticipationIdByUserId(
            @NotNull @PathVariable Long userId) {
        log.info("Запрос на участие: запрос на получение информации");
        validation.userIdValidation(userId);
        List<ParticipationRequestDto> participations = participationService.getParticipationByUserId(userId);
        log.info("Запросы на участие с id:{}", participations);
        return participations;

    }


    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequests(
            @RequestBody(required = false) EventRequestStatusUpdateRequest request,
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        log.info("Заявки: запрос на обновление статусов {}", request);

        EventRequestStatusUpdateResult result = participationService.updateRequests(eventId, userId, request);
        log.info("Статусы заявок обновлены");
        return result;
    }


    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequestsByUser(
            @PathVariable Long userId,
            @PathVariable Long requestId
    ) {
        log.info("Заявки: запрос на отмену заявки {}", requestId);
        validation.userIdValidation(userId);

        ParticipationRequestDto result = participationService.cancelRequestsByUser(requestId, userId);
        log.info("Заявка отменена {} ", result);
        return result;
    }


}
