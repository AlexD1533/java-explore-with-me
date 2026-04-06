package ru.practicum.ewm.compilation.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.compilation.Compilation;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventMapper;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    private final EventMapper eventMapper;

    public Compilation toCompilation(NewCompilationDto dto) {
        if (dto == null) {
            return null;
        }

        return Compilation.builder()
                .events(mapIdsToEvents(dto.events()))
                .pinned(dto.pinned())
                .title(dto.title())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        if (compilation == null) {
            return null;
        }

        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                mapEventsToShortDtos(compilation.getEvents())
        );
    }

    public void updateCompilationFromDto(UpdateCompilationRequest dto, Compilation compilation) {
        if (dto == null || compilation == null) {
            return;
        }

        if (dto.title() != null) {
            compilation.setTitle(dto.title());
        }
        if (dto.pinned() != null) {
            compilation.setPinned(dto.pinned());
        }
        if (dto.events() != null) {
            compilation.setEvents(mapIdsToEvents(dto.events()));
        }
    }

    private Set<Event> mapIdsToEvents(Set<Long> eventIds) {
        if (eventIds == null) {
            return null;
        }
        return eventIds.stream()
                .map(id -> Event.builder().id(id).build())
                .collect(Collectors.toSet());
    }

    private Set<EventShortDto> mapEventsToShortDtos(Set<Event> events) {
        if (events == null) {
            return Collections.emptySet();
        }
        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toSet());
    }
}