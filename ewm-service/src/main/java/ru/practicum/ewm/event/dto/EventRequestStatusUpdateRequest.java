package ru.practicum.ewm.event.dto;

import ru.practicum.ewm.user.RequestStatus;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        List<Long> requestIds,

        RequestStatus status
) {}
