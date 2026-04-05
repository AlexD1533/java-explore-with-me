package ru.practicum.ewm.statistic;

import ru.practicum.ewm.client.StatsClient;
import dto.EndpointHitDto;
import dto.ViewStatsDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatsClient statsClient;

    public void sendHit(String serviceName, HttpServletRequest request) {
        statsClient.saveHit(EndpointHitDto.NewEndpointHitDto.builder()
                .app(serviceName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<ViewStatsDto> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        return statsClient.getStats(start, end, uris, unique);
    }

    public Long getViews(Long eventId, HttpServletRequest request, List<ViewStatsDto> stats) {
        return stats.stream()
                .filter(s -> s.uri().equals(request.getRequestURI()))
                .map(ViewStatsDto::hits)
                .findFirst()
                .orElse(0L);

    }
}
