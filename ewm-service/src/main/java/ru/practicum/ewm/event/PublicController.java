package ru.practicum.ewm.event;


import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;


import java.util.List;

@RestController
@RequestMapping(path = "/events")
@Slf4j
@Validated
@RequiredArgsConstructor
public class PublicController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsByParamPublic(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size

    ) {
        log.info("События: запрос на получение информации");

        List<EventFullDto> events = eventService.searchEventsInfoByParmPublic(text, categories, paid, rangeStart, rangeEnd, from, size);
        log.info("Результат поиска: {}", events);
        return events;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByIdPublic(
            @NotNull @PathVariable Long eventId
    ) {
        log.info("Событие: запрос на получение информации");

        EventFullDto event = eventService.getEventByIdPublic(eventId);
        log.info("Результат поиска: {}", event);
        return event;
    }


}