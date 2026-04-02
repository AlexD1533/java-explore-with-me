package ru.practicum.ewm.compilation;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventService;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;

    public CompilationDto create(NewCompilationDto request) {

        Compilation newCompilation = compilationMapper.toCompilation(request);
        Set<Event> events = new HashSet<>(eventRepository.findAllById(request.events()));

        newCompilation.setEvents(events);
        return compilationMapper.toCompilationDto(compilationRepository.save(newCompilation));
    }
}
