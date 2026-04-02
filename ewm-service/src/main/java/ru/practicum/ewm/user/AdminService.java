package ru.practicum.ewm.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.event.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.validation.Validation;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final Validation validation;
    private final CategoryRepository categoryRepository;
    private final EventService eventService;


    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (request.eventDate() != null && event.getPublishedOn() != null) {
            validation.validateEventDateAdminUpdate(request.eventDate(), event.getPublishedOn());
        }

        validation.publicEventValidation(request, event);

        Category category = (request.category() != null)
                ? categoryRepository.findById(request.category())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"))
                : event.getCategory();

        event.setCategory(category);
        event.setConfirmedRequests(eventService.getConfirmedRequests(eventId));

        eventMapper.updateEventAdmin(request, event);

        Event result = eventRepository.save(event);

        System.out.println(result);
        System.out.println(eventMapper.toEventFullDto(result));

        return eventMapper.toEventFullDto(result);

    }

}
