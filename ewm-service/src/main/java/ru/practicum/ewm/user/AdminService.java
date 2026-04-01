package ru.practicum.ewm.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventMapper;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;



    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));



        System.out.println(event);
        eventMapper.updateEventAdmin(request, event);
        System.out.println(event);


        Event result = eventRepository.save(event);


        System.out.println(result);

        System.out.println(eventMapper.toEventFullDto(result));

        return eventMapper.toEventFullDto(result);

    }

}
