package ru.practicum.ewm.practicipation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RequestMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(source = "event.id", target = "event")
    @Mapping(source = "requester.id", target = "requester")
    @Mapping(source = "created", target = "created", qualifiedByName = "localDateTimeToString")
    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request);

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }
}