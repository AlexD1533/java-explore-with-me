package ru.practicum.ewm.compilation;

import ru.practicum.ewm.event.EventShortDto;
import ru.practicum.ewm.event.Event;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.ewm.event.EventMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = {EventMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class CompilationMapper {

    @Autowired
    protected EventMapper eventMapper;

    @Mapping(target = "events", expression = "java(mapEventIdsToEvents(dto.events()))")
    @Mapping(target = "id", ignore = true)
    public abstract Compilation toCompilation(NewCompilationDto dto);

    @Mapping(target = "events", expression = "java(mapEventsToShortDtos(compilation.getEvents()))")
    public abstract CompilationDto toCompilationDto(Compilation compilation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "events", expression = "java(dto.events() != null ? mapEventIdsToEvents(dto.events()) : compilation.getEvents())")
    public abstract void updateCompilationFromDto(UpdateCompilationRequest dto, @MappingTarget Compilation compilation);

    protected Set<Event> mapEventIdsToEvents(Set<Long> eventIds) {
        if (eventIds == null) return null;
        return eventIds.stream()
                .map(id -> Event.builder().id(id).build())
                .collect(Collectors.toSet());
    }

    protected abstract Set<EventShortDto> mapEventsToShortDtos(Set<Event> events);
}
