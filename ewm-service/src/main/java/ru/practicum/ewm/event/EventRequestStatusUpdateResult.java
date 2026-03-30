package ru.practicum.ewm.event;

import ru.practicum.ewm.practicipation.ParticipationRequestDto;

import java.util.List;

public record EventRequestStatusUpdateResult(
        List<ParticipationRequestDto> confirmedRequests,
        List<ParticipationRequestDto> rejectedRequests
) {}
