package ru.practicum.ewm.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.StateActionUser;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.location.LocationDto;
import ru.practicum.ewm.practicipation.ParticipationRepository;
import ru.practicum.ewm.practicipation.RequestStatus;
import ru.practicum.ewm.practicipation.ParticipationRequest;
import ru.practicum.ewm.statistic.StatisticService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class EventServiceIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @MockBean
    private StatisticService statisticService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private User testUser;
    private Category testCategory;
    private LocationDto testLocation;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .name("Test User")
                .email("test@example.com")
                .build());

        testCategory = categoryRepository.save(Category.builder()
                .name("Test Category")
                .build());

        testLocation = new LocationDto(55.7522f, 37.6156f);

        when(statisticService.getStatistic(any(), any(), any(), anyBoolean()))
                .thenReturn(List.of());
        doNothing().when(statisticService).sendHit(anyString(), any(HttpServletRequest.class));
        when(statisticService.getViews(any(HttpServletRequest.class), any()))
                .thenReturn(0L);
    }

    @Test
    void create_shouldSaveEventAndReturnFullDto() {
        // given
        String eventDate = LocalDateTime.now().plusHours(2).format(FORMATTER);
        NewEventDto dto = new NewEventDto(
                "Test Annotation Test Annotation",
                testCategory.getId(),
                "Test Description Test Description Test Description",
                eventDate,
                testLocation,
                true,
                100,
                true,
                "Test Title"
        );

        // when
        EventFullDto result = eventService.create(testUser.getId(), dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.annotation()).isEqualTo(dto.annotation());
        assertThat(result.description()).isEqualTo(dto.description());
        assertThat(result.title()).isEqualTo(dto.title());
        assertThat(result.paid()).isTrue();
        assertThat(result.participantLimit()).isEqualTo(100);
        assertThat(result.state()).isEqualTo(EventState.PENDING);
        assertThat(result.initiator().id()).isEqualTo(testUser.getId());
        assertThat(result.category().id()).isEqualTo(testCategory.getId());
        assertThat(result.location().lat()).isEqualTo(55.7522f);
    }

    @Test
    void getEventByIdByUserId_shouldReturnEventWhenExists() {
        // given
        Event event = createTestEvent(EventState.PUBLISHED);

        // when
        EventFullDto result = eventService.getEventByIdByUserId(testUser.getId(), event.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(event.getId());
        assertThat(result.annotation()).isEqualTo(event.getAnnotation());
    }

    @Test
    void getEventByIdByUserId_shouldThrowWhenEventNotFound() {
        // when & then
        assertThrows(NotFoundException.class, () ->
                eventService.getEventByIdByUserId(testUser.getId(), 999L)
        );
    }

    @Test
    void updateEvent_shouldUpdateFieldsCorrectly() {
        // given
        Event event = createTestEvent(EventState.PENDING);

        String newEventDate = LocalDateTime.now().plusDays(2).format(FORMATTER);
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest(
                "Updated Annotation Updated Annotation",
                null,
                "Updated Description Updated Description",
                newEventDate,
                new LocationDto(60.0f, 30.0f),
                false,
                50,
                false,
                null,
                "Updated Title"
        );

        // when
        EventFullDto result = eventService.updateEvent(event.getId(), testUser.getId(), updateRequest);

        // then
        assertThat(result.annotation()).isEqualTo("Updated Annotation Updated Annotation");
        assertThat(result.description()).isEqualTo("Updated Description Updated Description");
        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.paid()).isFalse();
        assertThat(result.participantLimit()).isEqualTo(50);
        assertThat(result.location().lat()).isEqualTo(60.0f);
    }

    @Test
    void updateEvent_shouldChangeStateToCanceled() {
        // given
        Event event = createTestEvent(EventState.PENDING);

        UpdateEventUserRequest cancelRequest = new UpdateEventUserRequest(
                null, null, null, null, null, null, null, null,
                StateActionUser.CANCEL_REVIEW,
                null
        );

        // when
        EventFullDto result = eventService.updateEvent(event.getId(), testUser.getId(), cancelRequest);

        // then
        assertThat(result.state()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void searchEventsByUserId_shouldReturnUserEventsWithPagination() {
        // given
        for (int i = 1; i <= 5; i++) {
            createTestEventWithTitle("Event " + i);
        }

        // when
        List<EventShortDto> result = eventService.searchEventsByUserId(testUser.getId(), 0, 2);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void searchEventsInfoByParm_shouldFilterByStateAndCategory() {
        // given
        Event publishedEvent = createTestEventWithState(EventState.PUBLISHED);
        Event pendingEvent = createTestEventWithState(EventState.PENDING);

        String start = LocalDateTime.now().minusDays(1).format(FORMATTER);
        String end = LocalDateTime.now().plusDays(7).format(FORMATTER);

        // when
        List<EventFullDto> result = eventService.searchEventsInfoByParm(
                List.of(testUser.getId()),
                List.of("PUBLISHED"),
                List.of(testCategory.getId()),
                start,
                end,
                0,
                10
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(publishedEvent.getId());
    }

    @Test
    void getConfirmedRequests_shouldReturnCorrectCount() {
        // given
        Event event = createTestEvent(EventState.PUBLISHED);

        User requester1 = userRepository.save(User.builder().name("User1").email("user1@test.com").build());
        User requester2 = userRepository.save(User.builder().name("User2").email("user2@test.com").build());

        participationRepository.save(createRequest(event, requester1, RequestStatus.CONFIRMED));
        participationRepository.save(createRequest(event, requester2, RequestStatus.CONFIRMED));
        participationRepository.save(createRequest(event,
                userRepository.save(User.builder().name("User3").email("user3@test.com").build()),
                RequestStatus.PENDING
        ));

        // when
        Long count = eventService.getConfirmedRequests(event.getId());

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void setConfirmedRequestsForList_shouldSetCountsForAllEvents() {
        // given
        Event event1 = createTestEvent(EventState.PUBLISHED);
        Event event2 = createTestEvent(EventState.PUBLISHED);

        User requester = userRepository.save(User.builder().name("Requester").email("req@test.com").build());
        participationRepository.save(createRequest(event1, requester, RequestStatus.CONFIRMED));
        participationRepository.save(createRequest(event1,
                userRepository.save(User.builder().name("R2").email("r2@test.com").build()),
                RequestStatus.CONFIRMED
        ));

        List<Event> events = List.of(event1, event2);

        // when
        List<Event> result = eventService.setConfirmedRequestsForList(events);

        // then
        assertThat(result.get(0).getConfirmedRequests()).isEqualTo(2L);
        assertThat(result.get(1).getConfirmedRequests()).isEqualTo(0L);
    }

    private Event createTestEvent(EventState state) {
        return eventRepository.save(Event.builder()
                .annotation("Test Annotation Test Annotation")
                .category(testCategory)
                .description("Test Description Test Description")
                .eventDate(LocalDateTime.now().plusHours(2))
                .location(ru.practicum.ewm.location.Location.builder().lat(55.75f).lon(37.61f).build())
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state(state)
                .title("Test Title")
                .initiator(testUser)
                .build());
    }

    private Event createTestEventWithTitle(String title) {
        return eventRepository.save(Event.builder()
                .annotation("Annotation for " + title + " xxxx")
                .category(testCategory)
                .description("Description for " + title + " xxxx")
                .eventDate(LocalDateTime.now().plusHours(2))
                .location(ru.practicum.ewm.location.Location.builder().lat(55.75f).lon(37.61f).build())
                .paid(false)
                .participantLimit(0)
                .requestModeration(false)
                .state(EventState.PUBLISHED)
                .title(title)
                .initiator(testUser)
                .build());
    }

    private Event createTestEventWithState(EventState state) {
        return createTestEvent(state);
    }

    private ParticipationRequest createRequest(Event event, User requester, RequestStatus status) {
        return ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .status(status)
                .build();
    }
}