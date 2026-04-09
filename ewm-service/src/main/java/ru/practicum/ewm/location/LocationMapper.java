package ru.practicum.ewm.location;

import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public Location toLocation(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        return Location.builder()
                .lat(dto.lat())
                .lon(dto.lon())
                .build();
    }

    public LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationDto(location.getLat(), location.getLon());
    }

    public void updateLocation(LocationDto dto, Location location) {
        if (dto == null || location == null) {
            return;
        }
        if (dto.lat() != null) {
            location.setLat(dto.lat());
        }
        if (dto.lon() != null) {
            location.setLon(dto.lon());
        }
    }
}