package ru.practicum.ewm.practicipation.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.practicipation.ParticipationRequest;
import ru.practicum.ewm.practicipation.RequestStatus;
import ru.practicum.ewm.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RequestParticipationMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        if (request == null) return null;

        return new ParticipationRequestDto(
                request.getCreated() != null ? request.getCreated().format(FORMATTER) : null,
                request.getEvent() != null ? request.getEvent().getId() : null,
                request.getId(),
                request.getRequester() != null ? request.getRequester().getId() : null,
                request.getStatus()
        );
    }

    public ParticipationRequest toParticipationRequest(Event event, Long userId) {
        if (event == null || userId == null) return null;

        return ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(User.builder().id(userId).build())
                .status(RequestStatus.PENDING)
                .build();
    }
}