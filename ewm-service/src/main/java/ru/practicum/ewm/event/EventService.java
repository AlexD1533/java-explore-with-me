package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryDto;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.practicipation.ParticipationRepository;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.validation.Validation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final Validation validation;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public EventFullDto create(Long userId, NewEventDto request) {

        Category category = categoryRepository.findById(request.category())
                .orElseThrow(() -> new NotFoundException("Категории не существует"));

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователя с id: " + "не существует"));

        Event newEvent = eventRepository.save(eventMapper.toEvent(request, category, user));
        System.out.println("event  " + newEvent);
        System.out.println(eventMapper.toEventFullDto(newEvent));
        return eventMapper.toEventFullDto(newEvent);

    }


    public EventFullDto getEventByIdByUserId(Long userId, Long eventId) {

        Event targetEvent = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Событие не найдено"));
        return eventMapper.toEventFullDto(targetEvent);
    }

    public EventFullDto updateEvent(Long eventId, Long userId, UpdateEventUserRequest request) {


        Event event = eventRepository.findEventForUpdate(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));


        validation.publicEventValidation(event);

        Category category = (request.category() != null)
                ? categoryRepository.findById(request.category())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"))
                : event.getCategory();

        event.setCategory(category);
        event.setConfirmedRequests(getConfirmedRequests(eventId));
        eventMapper.updateEventUser(request, event);

        Event result = eventRepository.save(event);
        return eventMapper.toEventFullDto(result);

    }

    public List<EventShortDto> searchEventsByUserId(@NotNull Long userId, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        return events.stream().map(eventMapper::toEventShortDto).toList();
    }

    public List<EventFullDto> searchEventsInfoByParm(List<Long> usersIds, List<String> states,
                                                     List<Long> categoryIds, String rangeStart,
                                                     String rangeEnd, Integer from, Integer size) {


        LocalDateTime start = (rangeStart != null) ? LocalDateTime.parse(rangeStart, formatter) : null;
        LocalDateTime end = (rangeEnd != null) ? LocalDateTime.parse(rangeEnd, formatter) : null;

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findAllEventsByParam(usersIds, states, categoryIds,
                start, end, pageable);

        return events.stream().map(eventMapper::toEventFullDto).toList();
    }

    public List<EventFullDto> searchEventsInfoByParmPulic(String text, List<Long> categories,
                                                          Boolean paid, String rangeStart, String rangeEnd, Integer from, Integer size) {

        LocalDateTime start = (rangeStart != null) ? LocalDateTime.parse(rangeStart, formatter) : null;
        LocalDateTime end = (rangeEnd != null) ? LocalDateTime.parse(rangeEnd, formatter) : null;

        Pageable pageable = PageRequest.of(from / size, size);


        List<Event> events = eventRepository.findAllEventsByParamPublic(text, categories, paid,
                start, end, pageable);

        return events.stream().map(eventMapper::toEventFullDto).toList();
    }

    public EventFullDto getEventByIdPublic(Long eventId) {
        Event targetEvent = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).orElseThrow(() ->
                new NotFoundException("Событие не найдено"));

        return eventMapper.toEventFullDto(targetEvent);
    }

    public Long getConfirmedRequests(Long eventId) {
        return participationRepository.countConfirmedRequests(eventId);

    }

}
