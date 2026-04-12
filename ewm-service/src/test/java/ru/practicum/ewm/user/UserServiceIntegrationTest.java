package ru.practicum.ewm.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.User;
import ru.practicum.ewm.UserRepository;
import ru.practicum.ewm.UserService;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_shouldSaveUserAndReturnDto() {
        // given
        NewUserRequest request = new NewUserRequest("John Doe", "john@example.com");

        // when
        UserDto result = userService.create(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john@example.com");

        // verify in DB
        User savedUser = userRepository.findById(result.id()).orElseThrow();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void delete_shouldRemoveUserFromDatabase() {
        // given
        User user = User.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .build();
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        // when
        userService.delete(userId);

        // then
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void searchUsersInfo_withoutIds_shouldReturnAllUsersWithPagination() {
        // given
        User user1 = userRepository.save(User.builder().name("User 1").email("user1@test.com").build());
        User user2 = userRepository.save(User.builder().name("User 2").email("user2@test.com").build());
        User user3 = userRepository.save(User.builder().name("User 3").email("user3@test.com").build());

        // when
        List<UserDto> result = userService.searchUsersInfo(null, 0, 2);

        // then
        assertThat(result).hasSize(2);
        // verify sorting by id ascending
        assertThat(result.get(0).id()).isLessThan(result.get(1).id());
    }

    @Test
    void searchUsersInfo_withSpecificIds_shouldReturnOnlyRequestedUsers() {
        // given
        User user1 = userRepository.save(User.builder().name("User 1").email("user1@test.com").build());
        User user2 = userRepository.save(User.builder().name("User 2").email("user2@test.com").build());
        User user3 = userRepository.save(User.builder().name("User 3").email("user3@test.com").build());

        List<Integer> requestedIds = List.of(user1.getId().intValue(), user3.getId().intValue());

        // when
        List<UserDto> result = userService.searchUsersInfo(requestedIds, 0, 10);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::id)
                .containsExactlyInAnyOrder(user1.getId(), user3.getId());
    }

    @Test
    void searchUsersInfo_withPagination_shouldReturnCorrectPage() {
        // given
        for (int i = 1; i <= 5; i++) {
            userRepository.save(User.builder()
                    .name("User " + i)
                    .email("ru/practicum/ewm/user" + i + "@test.com")
                    .build());
        }

        // when
        List<UserDto> firstPage = userService.searchUsersInfo(null, 0, 2);
        List<UserDto> secondPage = userService.searchUsersInfo(null, 2, 2);

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        assertThat(firstPage.get(0).id()).isLessThan(secondPage.get(0).id());
    }

    @Test
    void searchUsersInfo_withEmptyIdsList_shouldReturnAllUsers() {
        // given
        userRepository.save(User.builder().name("User 1").email("user1@test.com").build());
        userRepository.save(User.builder().name("User 2").email("user2@test.com").build());

        // when
        List<UserDto> result = userService.searchUsersInfo(List.of(), 0, 10);

        // then
        assertThat(result).hasSize(2);
    }
}