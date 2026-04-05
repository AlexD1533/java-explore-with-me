package ru.practicum.ewm.compilation;


import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;

import ru.practicum.ewm.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
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
        if (request.pinned() == null) {
            newCompilation.setPinned(false);
        }
        return compilationMapper.toCompilationDto(compilationRepository.save(newCompilation));
    }

    public void delete(Long compId) {

        compilationRepository.deleteById(compId);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборки с id: " + compId + "не существует"));

        if (!request.events().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(request.events()));
            compilation.setEvents(events);
        }
        compilationMapper.updateCompilationFromDto(request, compilation);
        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    public List<CompilationDto> searchCompilationInfoByParmPublic(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {

            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }

        return compilations.stream().map(compilationMapper::toCompilationDto).toList();

    }

    public CompilationDto getCompilationByIdPublic(Long compId) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборки с id: " + compId + "не существует"));
        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));

    }
}
