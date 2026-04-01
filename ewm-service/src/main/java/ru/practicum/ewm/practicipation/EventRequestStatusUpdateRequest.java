package ru.practicum.ewm.practicipation;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        List<Integer> requestIds,

        String status
) {}
