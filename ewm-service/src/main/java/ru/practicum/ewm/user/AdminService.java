package ru.practicum.ewm.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventMapper;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.validation.Validation;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final Validation validation;


    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        System.out.println("time! " + request.eventDate());

        if (request.eventDate() != null && event.getPublishedOn() != null) {
            validation.validateEventDateAdminUpdate(request.eventDate(), event.getPublishedOn());
        }

        System.out.println("asdf");
        validation.publicEventValidation(request, event);
        System.out.println("qwe");
        eventMapper.updateEventAdmin(request, event);
        System.out.println(event);


        Event result = eventRepository.save(event);

        System.out.println("zxc");

        System.out.println(result);
        System.out.println(eventMapper.toEventFullDto(result));

        return eventMapper.toEventFullDto(result);

    }

}
