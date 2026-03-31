package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.NewEventDto;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;


    public EventFullDto create(Long userId, NewEventDto request) {

        Event newEvent = eventRepository.save(eventMapper.toEvent(request, userId));
        return eventMapper.toEventFullDto(newEvent);

    }
}
