package ru.practicum.ewm.practicipation;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        List<Long> requestIds,

        RequestStatus status
) {}
