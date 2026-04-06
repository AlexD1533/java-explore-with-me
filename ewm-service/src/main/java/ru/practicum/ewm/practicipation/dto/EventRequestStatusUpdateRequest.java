package ru.practicum.ewm.practicipation.dto;

import lombok.Data;
import ru.practicum.ewm.practicipation.RequestStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds = new ArrayList<>();

    RequestStatus status;

}
