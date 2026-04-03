package ru.practicum.ewm.event;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.CompilationDto;
import ru.practicum.ewm.compilation.CompilationService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;


import java.util.List;

@RestController
@RequestMapping
@Slf4j
@Validated
@RequiredArgsConstructor
public class PublicController {

    private final EventService eventService;
    private final CompilationService compilationService;

    @GetMapping("/events")
    public List<EventFullDto> getEventsByParamPublic(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request

    ) {
        log.info("События: запрос на получение информации");

        List<EventFullDto> events = eventService.searchEventsInfoByParmPublic(text, categories, paid, rangeStart, rangeEnd, from, size, request);
        log.info("Результат поиска: {}", events);
        return events;
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEventByIdPublic(
            @NotNull @PathVariable Long eventId,
            HttpServletRequest request
    ) {
        log.info("Событие: запрос на получение информации");

        EventFullDto event = eventService.getEventByIdPublic(eventId, request);
        log.info("Результат поиска: {}", event);
        return event;
    }


    @GetMapping("/compilations")
    public List<CompilationDto> getCompilationsByParamPublic(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Подборки: запрос на получение информации");

        List<CompilationDto> compilations = compilationService.searchCompilationInfoByParmPublic(pinned, from, size);
        log.info("Результат поиска: {}", compilations);
        return compilations;
    }


    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilationByIdPublic(
            @NotNull @PathVariable Long compId
    ) {
        log.info("Подборка: запрос на получение информации");

        CompilationDto compilation = compilationService.getCompilationByIdPublic(compId);
        log.info("Результат поиска: {}", compilation);
        return compilation;
    }

}