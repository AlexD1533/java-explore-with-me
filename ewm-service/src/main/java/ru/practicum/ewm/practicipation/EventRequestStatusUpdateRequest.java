package ru.practicum.ewm.practicipation;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds = new ArrayList<>();

    RequestStatus status;

}
