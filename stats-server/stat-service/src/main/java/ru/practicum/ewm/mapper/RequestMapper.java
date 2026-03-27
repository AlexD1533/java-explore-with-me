package ru.practicum.ewm.mapper;

import dto.RequestInfoDto;

import ru.practicum.ewm.model.RequestInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    RequestInfoDto.FullRequestInfoDto toFullDto(RequestInfo entity);

    @Mapping(target = "id", ignore = true)
    RequestInfo toEntity(RequestInfoDto.NewRequestInfoDto dto);

}
