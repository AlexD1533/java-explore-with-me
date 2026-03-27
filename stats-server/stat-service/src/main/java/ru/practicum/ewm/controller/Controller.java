package ru.practicum.ewm.controller;

import dto.EndpointHitDto;
import dto.MessageResponse;
import dto.ViewStatsDto;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.model.EndpointHit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "")
@RequiredArgsConstructor
public class Controller {

    private final StatsService statsService;


    @PostMapping("/hit")
    public ResponseEntity<MessageResponse> saveRequestInfo(@Valid @RequestBody EndpointHitDto.NewEndpointHitDto request) {
        log.info("Запрос на сохранение информации о запросе {}", request);

        EndpointHit createEndpointHit = statsService.create(request);
        log.info("Информация сохранена, запрос с id={}", createEndpointHit.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse("Информация сохранена"));

    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getVisitInfo(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique
    ) {

        log.info("Запрос на получение статистики  посещений");
        return statsService.getVisitInfo(start, end, uris, unique);
    }


}
