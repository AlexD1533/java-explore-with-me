package ru.practicum.ewm.client;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class StatsClient {

    private final RestTemplate rest;
    private final String serverUrl;


    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.serverUrl = serverUrl;
        this.rest = builder
                .rootUri(serverUrl)
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();

    }

    public void saveHit(EndpointHitDto.NewEndpointHitDto hitDto) {
        rest.postForLocation("/hit", hitDto);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        // Формируем параметры запроса
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String startPar = (start == null) ? "" : start.format(formatter);
        String endPar = (end == null) ? "" : end.format(formatter);


        Map<String, Object> parameters = Map.of(
                "start", startPar,
                "end", endPar,
                "uris", String.join(",", uris),
                "unique", unique
        );

        String url = serverUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}";

        try {
            ResponseEntity<ViewStatsDto[]> response = rest.getForEntity(url, ViewStatsDto[].class, parameters);
            Object[] body = response.getBody();
            return body != null ? Arrays.asList((ViewStatsDto[]) body) : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }





}
