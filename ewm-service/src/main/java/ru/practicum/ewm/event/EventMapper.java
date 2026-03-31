package ru.practicum.ewm.event;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.location.LocationDto;
import ru.practicum.ewm.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Event toEvent(NewEventDto dto, Long userId) {
        return Event.builder()
                .annotation(dto.annotation())
                .category(Category.builder().id(dto.category()).build())
                .description(dto.description())
                .eventDate(LocalDateTime.parse(dto.eventDate(), FORMATTER))
                .location(toLocation(dto.location()))  // исправлено: locationDto()
                .paid(dto.paid() != null ? dto.paid() : false)
                .participantLimit(dto.participantLimit() != null ? dto.participantLimit() : 0)
                .requestModeration(dto.requestModeration() != null ? dto.requestModeration() : true)
                .state(EventState.PUBLISHED)
                .title(dto.title())
                .initiator(User.builder().id(userId).build())
                .build();
    }

    public EventFullDto toEventFullDto(Event event) {
        return new EventFullDto(
                event.getAnnotation(),
                null,
                event.getConfirmedRequests(),
                event.getCreatedOn() != null ? event.getCreatedOn().format(FORMATTER) : null,
                event.getDescription(),
                event.getEventDate().format(FORMATTER),
                event.getId(),
                null,
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
                null,
                event.getConfirmedRequests(),
                event.getEventDate().format(FORMATTER),
                event.getId(),
                null,
                event.getPaid(),
                event.getTitle(),
                event.getViews()
        );
    }

    public void updateEventFromDto(UpdateEventAdminRequest dto, Event event) {
        if (dto.annotation() != null) event.setAnnotation(dto.annotation());
        if (dto.category() != null) event.setCategory(Category.builder().id(dto.category()).build());
        if (dto.description() != null) event.setDescription(dto.description());
        if (dto.eventDate() != null) event.setEventDate(LocalDateTime.parse(dto.eventDate(), FORMATTER));
        if (dto.locationDto() != null) event.setLocation(toLocation(dto.locationDto()));  // исправлено
        if (dto.paid() != null) event.setPaid(dto.paid());
        if (dto.participantLimit() != null) event.setParticipantLimit(dto.participantLimit());
        if (dto.requestModeration() != null) event.setRequestModeration(dto.requestModeration());
        if (dto.title() != null) event.setTitle(dto.title());
    }

    public void updateEventFromDto(UpdateEventUserRequest dto, Event event) {
        if (dto.annotation() != null) event.setAnnotation(dto.annotation());
        if (dto.category() != null) event.setCategory(Category.builder().id(dto.category()).build());
        if (dto.description() != null) event.setDescription(dto.description());
        if (dto.eventDate() != null) event.setEventDate(LocalDateTime.parse(dto.eventDate(), FORMATTER));
        if (dto.locationDto() != null) event.setLocation(toLocation(dto.locationDto()));  // исправлено
        if (dto.paid() != null) event.setPaid(dto.paid());
        if (dto.participantLimit() != null) event.setParticipantLimit(dto.participantLimit());
        if (dto.requestModeration() != null) event.setRequestModeration(dto.requestModeration());
        if (dto.title() != null) event.setTitle(dto.title());
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
}