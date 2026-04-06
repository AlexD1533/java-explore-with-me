package ru.practicum.ewm.event;

import dto.ViewStatsDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.Category;

import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.practicipation.ParticipationRepository;
import ru.practicum.ewm.practicipation.ParticipationRequest;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.validation.Validation;
import ru.practicum.ewm.statistic.StatisticService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final Validation validation;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;

    private final String serviceName = "ewm-main-service";
    private final StatisticService statisticService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EventFullDto create(Long userId, NewEventDto request) {

        Category category = categoryRepository.findById(request.category())
                .orElseThrow(() -> new NotFoundException("Категории не существует"));

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователя с id: " + "не существует"));

        Event newEvent = eventRepository.save(eventMapper.toEvent(request, category, user));
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

    public List<EventShortDto> searchEventsByUserId(Long userId, Integer from, Integer size) {

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

        events = setConfirmedRequestsForList(events);

        return events.stream().map(eventMapper::toEventFullDto).toList();
    }

    public List<EventFullDto> searchEventsInfoByParmPublic(String text, List<Long> categories,
                                                           Boolean paid, String rangeStart, String rangeEnd, Integer from, Integer size, HttpServletRequest request) {
        LocalDateTime start;
        LocalDateTime end;

        if (rangeStart == null && rangeEnd == null) {
            start = LocalDateTime.now();
            end = null;
        } else {
            start = (rangeStart != null) ? LocalDateTime.parse(rangeStart, formatter) : null;
            end = (rangeEnd != null) ? LocalDateTime.parse(rangeEnd, formatter) : null;
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllEventsByParamPublic(text, categories, paid,
                start, end, pageable);

        List<ViewStatsDto> stats = statisticService.getStatistic(start, end, List.of(request.getRequestURI()), false);
        statisticService.sendHit(serviceName, request);

        events.forEach(s -> {
            s.setViews(statisticService.getViews(s.getId(), request, stats));
        });

        events = setConfirmedRequestsForList(events);

        return events.stream().map(eventMapper::toEventFullDto).toList();
    }

    public EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest request) {
        Event targetEvent = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).orElseThrow(() ->
                new NotFoundException("Событие не найдено"));

        List<ViewStatsDto> stats = statisticService.getStatistic(targetEvent.getCreatedOn(), LocalDateTime.now(), List.of(request.getRequestURI()), true);
        statisticService.sendHit(serviceName, request);

        targetEvent.setViews(statisticService.getViews(targetEvent.getId(), request, stats));
        targetEvent.setConfirmedRequests(getConfirmedRequests(eventId));

        eventRepository.save(targetEvent);
        System.out.println("qqq " + targetEvent);
        return eventMapper.toEventFullDto(targetEvent);
    }

    public Long getConfirmedRequests(Long eventId) {
        return participationRepository.countConfirmedRequests(eventId);
    }

    public List<Event> setConfirmedRequestsForList(List<Event> events) {

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> countsMap = participationRepository.findAllByEventIdsConfirmed(eventIds)
                .stream()
                .collect(Collectors.groupingBy(
                        r -> r.getEvent().getId(),
                        Collectors.counting()
                ));

        events.forEach(e -> e.setConfirmedRequests(countsMap.getOrDefault(e.getId(), 0L)));

        return events;
    }

}
