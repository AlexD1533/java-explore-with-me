package ru.practicum.ewm.practicipation;

import ru.practicum.ewm.user.RequestStatus;

public record ParticipationRequestDto(
        String created,
        Long event,
        Long id,
        Long requester,
        RequestStatus status
) {}
