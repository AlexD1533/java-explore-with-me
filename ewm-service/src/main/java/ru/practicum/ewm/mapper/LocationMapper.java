package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationMapper {

    Location toLocationDto(Location location);

    Location toLocation(Location location);
}
