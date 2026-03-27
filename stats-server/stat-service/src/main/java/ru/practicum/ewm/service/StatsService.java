package ru.practicum.ewm.service;

import dto.VisitInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.repository.VisitView;
import ru.practicum.ewm.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final RequestRepository requestRepository;

    public List<VisitInfoDto> getVisitInfo(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        List<VisitView> stats;
        if (unique) {
            stats = requestRepository.getVisitsStatisticDistinct(start, end, uris);
        } else {
            stats = requestRepository.getVisitsStatistic(start, end, uris);
        }

        return stats.stream()
                .map(view -> new VisitInfoDto(view.getApp(), view.getUri(), view.getHits()))
                .collect(Collectors.toList());
    }

}

