package ru.practicum.ewm.location;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class LocationMapperIntegrationTest {

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void toLocation_shouldMapDtoToEntity() {
        // given
        LocationDto dto = new LocationDto(55.7522f, 37.6156f);

        // when
        Location location = locationMapper.toLocation(dto);

        // then
        assertThat(location).isNotNull();
        assertThat(location.getId()).isNull(); // ID генерируется при сохранении
        assertThat(location.getLat()).isEqualTo(55.7522f);
        assertThat(location.getLon()).isEqualTo(37.6156f);
    }

    @Test
    void toLocation_withNull_shouldReturnNull() {
        // when
        Location location = locationMapper.toLocation(null);

        // then
        assertThat(location).isNull();
    }

    @Test
    void toLocationDto_shouldMapEntityToDto() {
        // given
        Location location = Location.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        // when
        LocationDto dto = locationMapper.toLocationDto(location);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.lat()).isEqualTo(55.7558f);
        assertThat(dto.lon()).isEqualTo(37.6173f);
    }

    @Test
    void toLocationDto_withNull_shouldReturnNull() {
        // when
        LocationDto dto = locationMapper.toLocationDto(null);

        // then
        assertThat(dto).isNull();
    }

    @Test
    void updateLocation_shouldUpdateAllFields() {
        // given
        Location location = locationRepository.save(Location.builder()
                .lat(50.0f)
                .lon(30.0f)
                .build());

        LocationDto updateDto = new LocationDto(60.0f, 40.0f);

        // when
        locationMapper.updateLocation(updateDto, location);

        // then
        assertThat(location.getLat()).isEqualTo(60.0f);
        assertThat(location.getLon()).isEqualTo(40.0f);

        // verify in DB
        Location updated = locationRepository.findById(location.getId()).orElseThrow();
        assertThat(updated.getLat()).isEqualTo(60.0f);
        assertThat(updated.getLon()).isEqualTo(40.0f);
    }

    @Test
    void updateLocation_withNullDto_shouldNotChangeEntity() {
        // given
        Location location = locationRepository.save(Location.builder()
                .lat(55.75f)
                .lon(37.61f)
                .build());

        Float originalLat = location.getLat();
        Float originalLon = location.getLon();

        // when
        locationMapper.updateLocation(null, location);

        // then
        assertThat(location.getLat()).isEqualTo(originalLat);
        assertThat(location.getLon()).isEqualTo(originalLon);
    }

    @Test
    void updateLocation_withNullLocation_shouldNotThrow() {
        // given
        LocationDto dto = new LocationDto(10.0f, 20.0f);

        // when & then - should not throw
        locationMapper.updateLocation(dto, null);
    }

    @Test
    void updateLocation_withNullLat_shouldNotUpdateLat() {
        // given
        Location location = locationRepository.save(Location.builder()
                .lat(55.75f)
                .lon(37.61f)
                .build());

        LocationDto partialUpdate = new LocationDto(null, 40.0f);

        // when
        locationMapper.updateLocation(partialUpdate, location);

        // then
        assertThat(location.getLat()).isEqualTo(55.75f); // не изменилось
        assertThat(location.getLon()).isEqualTo(40.0f);  // изменилось
    }

    @Test
    void updateLocation_withNullLon_shouldNotUpdateLon() {
        // given
        Location location = locationRepository.save(Location.builder()
                .lat(55.75f)
                .lon(37.61f)
                .build());

        LocationDto partialUpdate = new LocationDto(60.0f, null);

        // when
        locationMapper.updateLocation(partialUpdate, location);

        // then
        assertThat(location.getLat()).isEqualTo(60.0f);  // изменилось
        assertThat(location.getLon()).isEqualTo(37.61f); // не изменилось
    }

    @Test
    void fullMappingCycle_shouldPreserveData() {
        // given
        LocationDto originalDto = new LocationDto(59.9343f, 30.3351f); // координаты СПб

        // when
        Location entity = locationMapper.toLocation(originalDto);
        Location savedEntity = locationRepository.save(entity);
        LocationDto mappedBackDto = locationMapper.toLocationDto(savedEntity);

        // then
        assertThat(mappedBackDto.lat()).isEqualTo(originalDto.lat());
        assertThat(mappedBackDto.lon()).isEqualTo(originalDto.lon());
        assertThat(savedEntity.getId()).isNotNull();
    }

    @Test
    void updateLocation_afterSave_shouldPersistChanges() {
        // given
        LocationDto initialDto = new LocationDto(55.75f, 37.61f);
        Location location = locationMapper.toLocation(initialDto);
        Location saved = locationRepository.save(location);

        LocationDto updateDto = new LocationDto(59.93f, 30.33f);

        // when
        locationMapper.updateLocation(updateDto, saved);
        // явно сохраняем, т.к. маппер только меняет поля, но не сохраняет
        Location updated = locationRepository.save(saved);

        // then
        Location fromDb = locationRepository.findById(updated.getId()).orElseThrow();
        assertThat(fromDb.getLat()).isEqualTo(59.93f);
        assertThat(fromDb.getLon()).isEqualTo(30.33f);
    }
}