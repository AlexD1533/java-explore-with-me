package dto;

import model.enums.RequestStatus;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        List<Long> requestIds,

        RequestStatus status
) {}
