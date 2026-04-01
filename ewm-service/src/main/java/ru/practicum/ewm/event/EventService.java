package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.CategoryDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.exception.NotFoundException;
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
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EventFullDto create(Long userId, NewEventDto request) {

        Event newEvent = eventRepository.save(eventMapper.toEvent(request, userId));
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
        eventMapper.updateEventUser(request, event);
        Event result = eventRepository.save(event);
        return eventMapper.toEventFullDto(result);

    }

    public List<EventShortDto> searchEventsByUserId(@NotNull Long userId, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        return events.stream().map(eventMapper::toEventShortDto).toList();
    }

    public List<EventFullDto> searchEventsInfoByParm(List<Long> usersIds, List<String> stats,
                                                     List<Long> categoryIds, String rangeStart,
                                                     String rangeEnd, Integer from, Integer size) {

        System.out.println("qqq");

        LocalDateTime start = (rangeStart != null) ? LocalDateTime.parse(rangeStart, formatter) : null;
        LocalDateTime end = (rangeEnd != null) ? LocalDateTime.parse(rangeEnd, formatter) : null;
        System.out.println("www");
        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findAllEventsByParam(usersIds, stats, categoryIds,
                start, end, pageable);
        System.out.println("eee");
        return events.stream().map(eventMapper::toEventFullDto).toList();
    }
}
