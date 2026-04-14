package ru.practicum.ewm.mapper;

import dto.EndpointHitDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.EndpointHit;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class RequestMapperIntegrationTest {

    @Autowired
    private RequestMapper requestMapper;

    @Test
    void toEntity_shouldMapNewEndpointHitDtoToEntity() {
        // given
        LocalDateTime timestamp = LocalDateTime.now();
        EndpointHitDto.NewEndpointHitDto dto = new EndpointHitDto.NewEndpointHitDto(
                "ewm-main-service",
                "/events/100",
                "192.168.1.100",
                timestamp
        );

        // when
        EndpointHit entity = requestMapper.toEntity(dto);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getApp()).isEqualTo("ewm-main-service");
        assertThat(entity.getUri()).isEqualTo("/events/100");
        assertThat(entity.getIp()).isEqualTo("192.168.1.100");
        assertThat(entity.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void toFullDto_shouldMapEntityToDto() {
        // given
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        EndpointHit entity = new EndpointHit();
        entity.setId(1L);
        entity.setApp("stats-service");
        entity.setUri("/hit");
        entity.setIp("127.0.0.1");
        entity.setTimestamp(timestamp);

        // when
        EndpointHitDto.FullEndpointHitDto dto = requestMapper.toFullDto(entity);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.app()).isEqualTo("stats-service");
        assertThat(dto.uri()).isEqualTo("/hit");
        assertThat(dto.ip()).isEqualTo("127.0.0.1");
        assertThat(dto.timestamp()).isEqualTo(timestamp);
    }

    @Test
    void fullMappingCycle_shouldPreserveData() {
        // given
        LocalDateTime originalTimestamp = LocalDateTime.now();
        EndpointHitDto.NewEndpointHitDto newDto = new EndpointHitDto.NewEndpointHitDto(
                "test-app",
                "/api/test",
                "10.0.0.1",
                originalTimestamp
        );

        // when
        EndpointHit entity = requestMapper.toEntity(newDto);
        entity.setId(100L);
        EndpointHitDto.FullEndpointHitDto fullDto = requestMapper.toFullDto(entity);

        // then
        assertThat(fullDto.app()).isEqualTo(newDto.app());
        assertThat(fullDto.uri()).isEqualTo(newDto.uri());
        assertThat(fullDto.ip()).isEqualTo(newDto.ip());
        assertThat(fullDto.timestamp()).isEqualTo(originalTimestamp);
        assertThat(fullDto.id()).isEqualTo(100L);
    }
}