package ru.practicum.ewm.practicipation;

import jakarta.persistence.*;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.user.User;

import java.time.LocalDateTime;

public interface ViewRequest {

    Long getId();

    RequestStatus getStatus();

    Boolean getRequestModeration();

    Integer getParticipantLimit();
}
