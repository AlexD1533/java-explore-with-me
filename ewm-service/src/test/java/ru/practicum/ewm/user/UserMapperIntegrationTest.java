package ru.practicum.ewm.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserMapper;
import ru.practicum.ewm.user.dto.UserShortDto;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserMapperIntegrationTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void toUserDto_shouldMapEntityToDto() {
        // given
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        // when
        UserDto dto = userMapper.toUserDto(user);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Test User");
        assertThat(dto.email()).isEqualTo("test@example.com");
    }

    @Test
    void toUserDto_withNull_shouldReturnNull() {
        // when
        UserDto dto = userMapper.toUserDto(null);

        // then
        assertThat(dto).isNull();
    }

    @Test
    void toUser_shouldMapRequestToEntity() {
        // given
        NewUserRequest request = new NewUserRequest("New User", "new@example.com");

        // when
        User user = userMapper.toUser(request);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull(); // ID should be null for new entity
        assertThat(user.getName()).isEqualTo("New User");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void toUser_withNull_shouldReturnNull() {
        // when
        User user = userMapper.toUser(null);

        // then
        assertThat(user).isNull();
    }

    @Test
    void toUserShortDto_shouldMapEntityToShortDto() {
        // given
        User user = User.builder()
                .id(1L)
                .name("Short Name")
                .email("short@example.com")
                .build();

        // when
        UserShortDto shortDto = userMapper.toUserShortDto(user);

        // then
        assertThat(shortDto).isNotNull();
        assertThat(shortDto.id()).isEqualTo(1L);
        assertThat(shortDto.name()).isEqualTo("Short Name");
    }

    @Test
    void toUserShortDto_withNull_shouldReturnNull() {
        // when
        UserShortDto shortDto = userMapper.toUserShortDto(null);

        // then
        assertThat(shortDto).isNull();
    }

    @Test
    void fullMappingCycle_shouldWorkCorrectly() {
        // given
        NewUserRequest request = new NewUserRequest("Cycle Test", "cycle@example.com");

        // when
        User entity = userMapper.toUser(request);
        User savedEntity = userRepository.save(entity);
        UserDto dto = userMapper.toUserDto(savedEntity);

        // then
        assertThat(dto.name()).isEqualTo(request.name());
        assertThat(dto.email()).isEqualTo(request.email());
        assertThat(dto.id()).isNotNull();
    }
}