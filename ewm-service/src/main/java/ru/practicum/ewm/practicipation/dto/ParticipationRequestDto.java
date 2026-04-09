package ru.practicum.ewm.practicipation.dto;

import ru.practicum.ewm.practicipation.RequestStatus;

public record ParticipationRequestDto(
        String created,
        Long event,
        Long id,
        Long requester,
        RequestStatus status
) {
}
