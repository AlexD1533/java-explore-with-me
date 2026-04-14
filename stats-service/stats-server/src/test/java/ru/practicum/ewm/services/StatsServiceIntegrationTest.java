package ru.practicum.ewm.services;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatsService;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class StatsServiceIntegrationTest {

    @Autowired
    private StatsService statsService;

    @Autowired
    private StatsRepository statsRepository;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    void create_shouldSaveEndpointHit() {
        // given
        EndpointHitDto.NewEndpointHitDto dto = new EndpointHitDto.NewEndpointHitDto(
                "ewm-main-service",
                "/events/1",
                "192.168.1.1",
                now
        );

        // when
        EndpointHit result = statsService.create(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getApp()).isEqualTo("ewm-main-service");
        assertThat(result.getUri()).isEqualTo("/events/1");
        assertThat(result.getIp()).isEqualTo("192.168.1.1");
        assertThat(result.getTimestamp()).isEqualTo(now);
    }

    @Test
    void getVisitInfo_withoutUnique_shouldCountAllRequests() {
        // given
        saveHit("ewm-service", "/events/1", "192.168.1.1", now);
        saveHit("ewm-service", "/events/1", "192.168.1.1", now.plusMinutes(1));
        saveHit("ewm-service", "/events/1", "192.168.1.2", now.plusMinutes(2));
        saveHit("ewm-service", "/events/2", "192.168.1.1", now);

        // when
        List<ViewStatsDto> result = statsService.getVisitInfo(
                now.minusHours(1),
                now.plusHours(1),
                List.of("/events/1"),
                false
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).hits()).isEqualTo(3L);
        assertThat(result.get(0).uri()).isEqualTo("/events/1");
    }

    @Test
    void getVisitInfo_withUnique_shouldCountDistinctIps() {
        // given
        saveHit("ewm-service", "/events/1", "192.168.1.1", now);
        saveHit("ewm-service", "/events/1", "192.168.1.1", now.plusMinutes(1));
        saveHit("ewm-service", "/events/1", "192.168.1.2", now.plusMinutes(2));

        // when
        List<ViewStatsDto> result = statsService.getVisitInfo(
                now.minusHours(1),
                now.plusHours(1),
                List.of("/events/1"),
                true
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).hits()).isEqualTo(2L);
    }

    @Test
    void getVisitInfo_withoutUrisFilter_shouldReturnAllUris() {
        // given
        saveHit("ewm-service", "/events/1", "192.168.1.1", now);
        saveHit("ewm-service", "/events/2", "192.168.1.1", now);
        saveHit("ewm-service", "/categories", "192.168.1.1", now);

        // when
        List<ViewStatsDto> result = statsService.getVisitInfo(
                now.minusHours(1),
                now.plusHours(1),
                null,
                false
        );

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(ViewStatsDto::uri)
                .containsExactlyInAnyOrder("/events/1", "/events/2", "/categories");
    }

    @Test
    void getVisitInfo_withTimeRange_shouldReturnOnlyHitsInRange() {
        // given
        saveHit("ewm-service", "/events/1", "192.168.1.1", now.minusHours(2));
        saveHit("ewm-service", "/events/1", "192.168.1.1", now);
        saveHit("ewm-service", "/events/1", "192.168.1.1", now.plusHours(2));

        // when
        List<ViewStatsDto> result = statsService.getVisitInfo(
                now.minusMinutes(30),
                now.plusMinutes(30),
                List.of("/events/1"),
                false
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).hits()).isEqualTo(1L);
    }

    @Test
    void getVisitInfo_shouldOrderByHitsDesc() {
        // given
        for (int i = 0; i < 5; i++) {
            saveHit("ewm-service", "/events/1", "192.168.1." + i, now);
        }
        for (int i = 0; i < 3; i++) {
            saveHit("ewm-service", "/events/2", "192.168.2." + i, now);
        }
        for (int i = 0; i < 10; i++) {
            saveHit("ewm-service", "/categories", "192.168.3." + i, now);
        }

        // when
        List<ViewStatsDto> result = statsService.getVisitInfo(
                now.minusHours(1),
                now.plusHours(1),
                null,
                false
        );

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).uri()).isEqualTo("/categories");
        assertThat(result.get(1).uri()).isEqualTo("/events/1");
        assertThat(result.get(2).uri()).isEqualTo("/events/2");
    }

    private void saveHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHit hit = new EndpointHit();
        hit.setApp(app);
        hit.setUri(uri);
        hit.setIp(ip);
        hit.setTimestamp(timestamp);
        statsRepository.save(hit);
    }
}