package ru.practicum.ewm.user.dto.practicipation.dto;

import ru.practicum.ewm.user.dto.practicipation.RequestStatus;

public record ParticipationRequestDto(
        String created,
        Long event,
        Long id,
        Long requester,
        RequestStatus status
) {
}
