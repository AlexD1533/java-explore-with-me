package ru.practicum.ewm.practicipation;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final PracticipationRepository practicipationRepository;
    private final RequestMapper requestMapper;

    public List<ParticipationRequestDto> getParticipationByUserIdAndEventId(Long userId, Long eventId) {

        List<ParticipationRequest> participationRequest = practicipationRepository.findAllByEventIdAndRequesterId(eventId, userId);
        return participationRequest.stream().map(requestMapper::toParticipationRequestDto).toList();
    }
}
