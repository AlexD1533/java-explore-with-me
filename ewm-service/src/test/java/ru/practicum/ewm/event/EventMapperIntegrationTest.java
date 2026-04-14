package ru.practicum.ewm.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.StateActionAdmin;
import ru.practicum.ewm.user.StateActionUser;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.location.LocationDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class EventMapperIntegrationTest {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Category category;
    private User user;
    private LocationDto locationDto;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(Category.builder().name("Test Category").build());
        user = userRepository.save(User.builder().name("Test User").email("test@test.com").build());
        locationDto = new LocationDto(55.7558f, 37.6173f);
    }

    @Test
    void toEvent_shouldMapNewEventDtoToEntity() {
        // given
        String eventDate = LocalDateTime.now().plusHours(3).format(FORMATTER);
        NewEventDto dto = new NewEventDto(
                "Test Annotation Test Annotation",
                category.getId(),
                "Test Description Test Description",
                eventDate,
                locationDto,
                true,
                100,
                true,
                "Test Title"
        );

        // when
        Event event = eventMapper.toEvent(dto, category, user);

        // then
        assertThat(event).isNotNull();
        assertThat(event.getId()).isNull(); // Новое событие без ID
        assertThat(event.getAnnotation()).isEqualTo(dto.annotation());
        assertThat(event.getDescription()).isEqualTo(dto.description());
        assertThat(event.getTitle()).isEqualTo(dto.title());
        assertThat(event.getPaid()).isTrue();
        assertThat(event.getParticipantLimit()).isEqualTo(100);
        assertThat(event.getRequestModeration()).isTrue();
        assertThat(event.getState()).isEqualTo(EventState.PENDING);
        assertThat(event.getCategory()).isEqualTo(category);
        assertThat(event.getInitiator()).isEqualTo(user);
        assertThat(event.getLocation().getLat()).isEqualTo(55.7558f);
        assertThat(event.getEventDate()).isEqualTo(LocalDateTime.parse(eventDate, FORMATTER));
    }

    @Test
    void toEvent_shouldSetDefaultValues() {
        // given
        String eventDate = LocalDateTime.now().plusHours(1).format(FORMATTER);
        NewEventDto dtoWithNulls = new NewEventDto(
                "Annotation Annotation",
                category.getId(),
                "Description Description",
                eventDate,
                locationDto,
                null, // paid = null -> false
                null, // participantLimit = null -> 0
                null, // requestModeration = null -> true
                "Title"
        );

        // when
        Event event = eventMapper.toEvent(dtoWithNulls, category, user);

        // then
        assertThat(event.getPaid()).isFalse();
        assertThat(event.getParticipantLimit()).isEqualTo(0);
        assertThat(event.getRequestModeration()).isTrue();
    }

    @Test
    void toEventFullDto_shouldMapEntityToDto() {
        // given
        Event event = createTestEvent();

        // when
        EventFullDto dto = eventMapper.toEventFullDto(event);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(event.getId());
        assertThat(dto.annotation()).isEqualTo(event.getAnnotation());
        assertThat(dto.description()).isEqualTo(event.getDescription());
        assertThat(dto.title()).isEqualTo(event.getTitle());
        assertThat(dto.state()).isEqualTo(EventState.PENDING);
        assertThat(dto.category().id()).isEqualTo(category.getId());
        assertThat(dto.initiator().id()).isEqualTo(user.getId());
        assertThat(dto.location().lat()).isEqualTo(55.7558f);
        assertThat(dto.eventDate()).isEqualTo(event.getEventDate().format(FORMATTER));
    }

    @Test
    void toEventShortDto_shouldMapEntityToShortDto() {
        // given
        Event event = createTestEvent();

        // when
        EventShortDto dto = eventMapper.toEventShortDto(event);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(event.getId());
        assertThat(dto.annotation()).isEqualTo(event.getAnnotation());
        assertThat(dto.title()).isEqualTo(event.getTitle());
        assertThat(dto.category().id()).isEqualTo(category.getId());
        assertThat(dto.initiator().id()).isEqualTo(user.getId());
        assertThat(dto.paid()).isEqualTo(event.getPaid());
    }

    @Test
    void updateEventUser_shouldUpdateAllFields() {
        // given
        Event event = createTestEvent();
        String newDate = LocalDateTime.now().plusDays(5).format(FORMATTER);

        UpdateEventUserRequest request = new UpdateEventUserRequest(
                "Updated Annotation",
                null,
                "Updated Description",
                newDate,
                new LocationDto(60.0f, 30.0f),
                false,
                200,
                false,
                null,
                "Updated Title"
        );

        // when
        eventMapper.updateEventUser(request, event);

        // then
        assertThat(event.getAnnotation()).isEqualTo("Updated Annotation");
        assertThat(event.getDescription()).isEqualTo("Updated Description");
        assertThat(event.getTitle()).isEqualTo("Updated Title");
        assertThat(event.getPaid()).isFalse();
        assertThat(event.getParticipantLimit()).isEqualTo(200);
        assertThat(event.getRequestModeration()).isFalse();
        assertThat(event.getLocation().getLat()).isEqualTo(60.0f);
    }

    @Test
    void updateEventUser_shouldChangeStateToCanceled() {
        // given
        Event event = createTestEvent();
        event.setState(EventState.PENDING);

        UpdateEventUserRequest request = new UpdateEventUserRequest(
                null, null, null, null, null, null, null, null,
                StateActionUser.CANCEL_REVIEW,
                null
        );

        // when
        eventMapper.updateEventUser(request, event);

        // then
        assertThat(event.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void updateEventUser_shouldChangeStateToPending() {
        // given
        Event event = createTestEvent();
        event.setState(EventState.CANCELED);

        UpdateEventUserRequest request = new UpdateEventUserRequest(
                null, null, null, null, null, null, null, null,
                StateActionUser.SEND_TO_REVIEW,
                null
        );

        // when
        eventMapper.updateEventUser(request, event);

        // then
        assertThat(event.getState()).isEqualTo(EventState.PENDING);
    }

    @Test
    void updateEventAdmin_shouldPublishEvent() {
        // given
        Event event = createTestEvent();
        event.setState(EventState.PENDING);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest(
                null, null, null, null, null, null, null, null,
                StateActionAdmin.PUBLISH_EVENT,
                null
        );

        // when
        eventMapper.updateEventAdmin(request, event);

        // then
        assertThat(event.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(event.getPublishedOn()).isNotNull();
    }

    @Test
    void updateEventAdmin_shouldRejectEvent() {
        // given
        Event event = createTestEvent();
        event.setState(EventState.PENDING);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest(
                null, null, null, null, null, null, null, null,
                StateActionAdmin.REJECT_EVENT,
                null
        );

        // when
        eventMapper.updateEventAdmin(request, event);

        // then
        assertThat(event.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void fullMappingCycle_shouldPreserveData() {
        // given
        String eventDate = LocalDateTime.now().plusHours(4).format(FORMATTER);
        NewEventDto newDto = new NewEventDto(
                "Original Annotation",
                category.getId(),
                "Original Description",
                eventDate,
                locationDto,
                true,
                50,
                true,
                "Original Title"
        );

        // when
        Event entity = eventMapper.toEvent(newDto, category, user);
        EventFullDto fullDto = eventMapper.toEventFullDto(entity);

        // then
        assertThat(fullDto.annotation()).isEqualTo(newDto.annotation());
        assertThat(fullDto.description()).isEqualTo(newDto.description());
        assertThat(fullDto.title()).isEqualTo(newDto.title());
        assertThat(fullDto.paid()).isEqualTo(newDto.paid());
    }

    private Event createTestEvent() {
        return Event.builder()
                .annotation("Test Annotation Test Annotation")
                .category(category)
                .description("Test Description Test Description")
                .eventDate(LocalDateTime.now().plusHours(2))
                .location(ru.practicum.ewm.location.Location.builder().lat(55.7558f).lon(37.6173f).build())
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state(EventState.PENDING)
                .title("Test Title")
                .initiator(user)
                .build();
    }
}