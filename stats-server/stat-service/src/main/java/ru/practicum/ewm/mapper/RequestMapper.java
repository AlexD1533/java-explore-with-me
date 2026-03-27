package ru.practicum.ewm.mapper;

import dto.EndpointHitDto;

import ru.practicum.ewm.model.EndpointHit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    EndpointHitDto.FullEndpointHitDto toFullDto(EndpointHit entity);

    @Mapping(target = "id", ignore = true)
    EndpointHit toEntity(EndpointHitDto.NewEndpointHitDto dto);

}
