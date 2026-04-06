package ru.practicum.ewm.user.dto.practicipation.dto;

import lombok.Data;
import ru.practicum.ewm.user.dto.practicipation.RequestStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds = new ArrayList<>();

    RequestStatus status;

}
