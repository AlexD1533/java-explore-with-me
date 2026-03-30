package ru.practicum.ewm.dto;

import ru.practicum.ewm.model.enums.RequestStatus;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        List<Long> requestIds,

        RequestStatus status
) {}
