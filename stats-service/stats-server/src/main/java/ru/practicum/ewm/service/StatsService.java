package ru.practicum.ewm.service;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.repository.ViewStats;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository statsRepository;
    private final RequestMapper requestMapper;

    public EndpointHit create(EndpointHitDto.NewEndpointHitDto request) {
        return statsRepository.save(requestMapper.toEntity(request));
    }

    public List<ViewStatsDto> getVisitInfo(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {


        List<ViewStats> stats;
        if (unique) {
            stats = statsRepository.getVisitsStatisticDistinct(start, end, uris);
        } else {
            stats = statsRepository.getVisitsStatistic(start, end, uris);
        }

        return stats.stream()
                .map(view -> new ViewStatsDto(view.getApp(), view.getUri(), view.getHits()))
                .collect(Collectors.toList());
    }
}

