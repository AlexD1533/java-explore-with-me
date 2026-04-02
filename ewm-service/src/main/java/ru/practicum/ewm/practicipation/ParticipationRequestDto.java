package ru.practicum.ewm.practicipation;

public record ParticipationRequestDto(
        String created,
        Long event,
        Long id,
        Long requester,
        RequestStatus status
) {
}
