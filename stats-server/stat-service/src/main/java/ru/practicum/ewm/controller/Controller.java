package ru.practicum.ewm.controller;

import dto.MessageResponse;
import dto.RequestInfoDto;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.RequestInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "")
@RequiredArgsConstructor
public class Controller {

    private final RequestService requestService;


    @PostMapping("/hit")
    public ResponseEntity<MessageResponse> saveRequestInfo(@Valid @RequestBody RequestInfoDto.NewRequestInfoDto request) {
        log.info("Запрос на сохранение информации о запросе {}", request);

        RequestInfo createRequestInfo = requestService.create(request);
        log.info("Информация сохранена, запрос с id={}", createRequestInfo.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse("Информация сохранена"));

    }


}
