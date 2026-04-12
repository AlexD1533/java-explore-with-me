package ru.practicum.ewm.client;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(StatsClient.class)
class StatsClientMockServerTest {

    @Autowired
    private StatsClient statsClient;

    @Autowired
    private MockRestServiceServer mockServer;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        mockServer.reset();
    }

    @Test
    void saveHit_shouldSendPostToHitEndpoint() {
        // given
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        EndpointHitDto.NewEndpointHitDto hitDto = new EndpointHitDto.NewEndpointHitDto(
                "ewm-main-service",
                "/events/1",
                "192.168.1.1",
                timestamp
        );

        mockServer.expect(requestTo("/hit"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andRespond(withStatus(HttpStatus.CREATED));

        // when
        statsClient.saveHit(hitDto);

        // then
        mockServer.verify();
    }

    @Test
    void getStats_shouldSendGetWithQueryParams() {
        // given
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        List<String> uris = List.of("/events/1", "/events/2");

        // Формируем ожидаемый URL с кодированием параметров (пробелы -> %20)
        String expectedUrl = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("uris", String.join(",", uris))
                .queryParam("unique", true)
                .toUriString();

        String responseJson = """
                [
                    {"app": "ewm-main-service", "uri": "/events/1", "hits": 10},
                    {"app": "ewm-main-service", "uri": "/events/2", "hits": 5}
                ]
                """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // when
        List<ViewStatsDto> result = statsClient.getStats(start, end, uris, true);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).uri()).isEqualTo("/events/1");
        assertThat(result.get(0).hits()).isEqualTo(10L);
        mockServer.verify();
    }

    @Test
    void getStats_withNullDates_shouldSendEmptyStrings() {
        // given
        List<String> uris = List.of("/events/1");

        String expectedUrl = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", "")
                .queryParam("end", "")
                .queryParam("uris", "/events/1")
                .queryParam("unique", false)
                .toUriString();

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        // when
        List<ViewStatsDto> result = statsClient.getStats(null, null, uris, false);

        // then
        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void getStats_whenServerError_shouldReturnEmptyList() {
        // given
        LocalDateTime start = LocalDateTime.now();

        // Используем startsWith для гибкости
        mockServer.expect(requestTo(startsWith("/stats?")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        // when
        List<ViewStatsDto> result = statsClient.getStats(start, start.plusHours(1), List.of("/test"), false);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getStats_whenNotFound_shouldReturnEmptyList() {
        // given
        LocalDateTime start = LocalDateTime.now();

        mockServer.expect(requestTo(startsWith("/stats?")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // when
        List<ViewStatsDto> result = statsClient.getStats(start, start.plusDays(1), List.of("/test"), false);

        // then
        assertThat(result).isEmpty();
    }
}