package ru.practicum.ewm.dto;

import ru.practicum.ewm.model.enums.RequestStatus;

public record ParticipationRequestDto(
        String created,
        Long event,
        Long id,
        Long requester,
        RequestStatus status
) {}
