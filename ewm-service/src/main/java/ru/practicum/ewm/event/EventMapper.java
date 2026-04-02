package ru.practicum.ewm.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.location.LocationDto;
import ru.practicum.ewm.practicipation.ParticipationRepository;
import ru.practicum.ewm.user.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Event toEvent(NewEventDto dto, Category category, User user) {


        return Event.builder()
                .annotation(dto.annotation())
                .category(category)
                .description(dto.description())
                .eventDate(LocalDateTime.parse(dto.eventDate(), FORMATTER))
                .location(toLocation(dto.location()))
                .paid(dto.paid() != null ? dto.paid() : false)
                .participantLimit(dto.participantLimit() != null ? dto.participantLimit() : 0)
                .requestModeration(dto.requestModeration() != null ? dto.requestModeration() : true)
                .state(EventState.PENDING)
                .title(dto.title())
                .initiator(user)
                .build();
    }

    public EventFullDto toEventFullDto(Event event) {

        return new EventFullDto(
                event.getAnnotation(),
                categoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getCreatedOn() != null ? event.getCreatedOn().format(FORMATTER) : null,
                event.getDescription(),
                event.getEventDate().format(FORMATTER),
                event.getId(),
                userMapper.toUserShortDto(event.getInitiator()),
                toLocationDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn() != null ? event.getPublishedOn().format(FORMATTER) : null,
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                event.getViews()
        );
    }

    public EventShortDto toEventShortDto(Event event) {
        return new EventShortDto(
                event.getAnnotation(),
                categoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getEventDate().format(FORMATTER),
                event.getId(),
                userMapper.toUserShortDto(event.getInitiator()),
                event.getPaid(),
                event.getTitle(),
                event.getViews()
        );
    }

    public void updateEventUser(UpdateEventUserRequest dto, Event event) {

        if (dto.annotation() != null) event.setAnnotation(dto.annotation());
        if (dto.description() != null) event.setDescription(dto.description());
        if (dto.eventDate() != null) event.setEventDate(LocalDateTime.parse(dto.eventDate(), FORMATTER));
        if (dto.location() != null) event.setLocation(toLocation(dto.location()));
        if (dto.paid() != null) event.setPaid(dto.paid());
        if (dto.participantLimit() != null) event.setParticipantLimit(dto.participantLimit());
        if (dto.requestModeration() != null) event.setRequestModeration(dto.requestModeration());
        if (dto.title() != null) event.setTitle(dto.title());
        if (dto.stateAction() != null) {
            if (dto.stateAction().equals(StateActionUser.CANCEL_REVIEW)) event.setState(EventState.CANCELED);
            if (dto.stateAction().equals(StateActionUser.SEND_TO_REVIEW)) event.setState(EventState.PENDING);

        }
    }

    private Location toLocation(LocationDto dto) {
        if (dto == null) return null;
        return Location.builder()
                .lat(dto.lat())
                .lon(dto.lon())
                .build();
    }

    private LocationDto toLocationDto(Location location) {
        if (location == null) return null;
        return new LocationDto(location.getLat(), location.getLon());
    }

    public void updateEventAdmin(UpdateEventAdminRequest dto, Event event) {

        if (dto.annotation() != null) event.setAnnotation(dto.annotation());
        if (dto.description() != null) event.setDescription(dto.description());
        if (dto.eventDate() != null) event.setEventDate(LocalDateTime.parse(dto.eventDate(), FORMATTER));
        if (dto.location() != null) event.setLocation(toLocation(dto.location()));
        if (dto.paid() != null) event.setPaid(dto.paid());
        if (dto.participantLimit() != null) event.setParticipantLimit(dto.participantLimit());
        if (dto.requestModeration() != null) event.setRequestModeration(dto.requestModeration());
        if (dto.title() != null) event.setTitle(dto.title());
        if (dto.stateAction() != null) {
            if (dto.stateAction().equals(StateActionAdmin.REJECT_EVENT)) event.setState(EventState.CANCELED);
            if (dto.stateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
        }
    }
}