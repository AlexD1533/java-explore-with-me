package ru.practicum.ewm.location;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationMapper {

    LocationDto toLocationDto(LocationDto locationDto);

    LocationDto toLocation(LocationDto locationDto);
}
