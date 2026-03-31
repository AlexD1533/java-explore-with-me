package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.CategoryDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;


    public EventFullDto create(Long userId, NewEventDto request) {

        Event newEvent = eventRepository.save(eventMapper.toEvent(request, userId));
        return eventMapper.toEventFullDto(newEvent);

    }

    public EventFullDto getEventByIdByUserId(Long userId, Long eventId) {

        Event targetEvent = eventRepository.findByIdAndInitiatorId(userId, eventId).orElseThrow(() ->
               new NotFoundException("Событие не найдено"));

        return eventMapper.toEventFullDto(targetEvent);
    }

    public EventFullDto updateEvent(Long eventId, Long userId, UpdateEventUserRequest request) {

        Event event = eventRepository.findEventForUpdate(eventId, userId).orElseThrow(() ->
                new NotFoundException("Событие не найдено"));

        eventMapper.updateEventFromDto(request, event);

        return eventMapper.toEventFullDto(eventRepository.save(event));

    }
}
