package ru.practicum.ewm.participation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.practicipation.ParticipationRequest;
import ru.practicum.ewm.practicipation.RequestStatus;
import ru.practicum.ewm.practicipation.dto.ParticipationRequestDto;
import ru.practicum.ewm.practicipation.dto.RequestParticipationMapper;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class RequestParticipationMapperIntegrationTest {

    @Autowired
    private RequestParticipationMapper mapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private User initiator;
    private User requester;
    private Category category;

    @BeforeEach
    void setUp() {
        initiator = userRepository.save(User.builder()
                .name("Initiator")
                .email("initiator@test.com")
                .build());

        requester = userRepository.save(User.builder()
                .name("Requester")
                .email("requester@test.com")
                .build());

        category = categoryRepository.save(Category.builder()
                .name("Test Category")
                .build());
    }

    @Test
    void toParticipationRequestDto_shouldMapAllFields() {
        // given
        Event event = eventRepository.save(Event.builder()
                .annotation("Annotation Annotation")
                .category(category)
                .description("Description Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(Location.builder().lat(55.75f).lon(37.61f).build())
                .paid(false)
                .participantLimit(100)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .title("Test Event")
                .initiator(initiator)
                .build());

        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .created(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();

        // when
        ParticipationRequestDto dto = mapper.toParticipationRequestDto(request);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.created()).isEqualTo("2024-01-15 10:30:00");
        assertThat(dto.event()).isEqualTo(event.getId());
        assertThat(dto.requester()).isEqualTo(requester.getId());
        assertThat(dto.status()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void toParticipationRequestDto_withNullCreated_shouldHandleGracefully() {
        // given
        Event event = createEvent();

        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .created(null)
                .event(event)
                .requester(requester)
                .status(RequestStatus.CONFIRMED)
                .build();

        // when
        ParticipationRequestDto dto = mapper.toParticipationRequestDto(request);

        // then
        assertThat(dto.created()).isNull();
        assertThat(dto.status()).isEqualTo(RequestStatus.CONFIRMED);
    }

    @Test
    void toParticipationRequestDto_withNullEvent_shouldHandleGracefully() {
        // given
        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(null)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();

        // when
        ParticipationRequestDto dto = mapper.toParticipationRequestDto(request);

        // then
        assertThat(dto.event()).isNull();
        assertThat(dto.requester()).isEqualTo(requester.getId());
    }

    @Test
    void toParticipationRequestDto_withNullRequester_shouldHandleGracefully() {
        // given
        Event event = createEvent();

        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(event)
                .requester(null)
                .status(RequestStatus.REJECTED)
                .build();

        // when
        ParticipationRequestDto dto = mapper.toParticipationRequestDto(request);

        // then
        assertThat(dto.requester()).isNull();
        assertThat(dto.event()).isEqualTo(event.getId());
    }

    @Test
    void toParticipationRequestDto_withNull_shouldReturnNull() {
        // when
        ParticipationRequestDto dto = mapper.toParticipationRequestDto(null);

        // then
        assertThat(dto).isNull();
    }

    @Test
    void toParticipationRequest_shouldCreateRequestWithPendingStatus() {
        // given
        Event event = createEvent();
        Long userId = 999L; // Можно использовать несуществующий ID, т.к. маппер просто создает объект, а не сохраняет

        // when
        ParticipationRequest request = mapper.toParticipationRequest(event, userId);

        // then
        assertThat(request).isNotNull();
        assertThat(request.getId()).isNull(); // ID генерируется БД
        assertThat(request.getCreated()).isNotNull(); // Текущее время
        assertThat(request.getEvent()).isEqualTo(event);
        assertThat(request.getRequester().getId()).isEqualTo(userId);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void toParticipationRequest_withNullEvent_shouldReturnNull() {
        // when
        ParticipationRequest request = mapper.toParticipationRequest(null, 1L);

        // then
        assertThat(request).isNull();
    }

    @Test
    void toParticipationRequest_withNullUserId_shouldReturnNull() {
        // given
        Event event = createEvent();

        // when
        ParticipationRequest request = mapper.toParticipationRequest(event, null);

        // then
        assertThat(request).isNull();
    }

    @Test
    void fullMappingCycle_shouldPreserveData() {
        // given
        Event event = createEvent();
        Long userId = requester.getId();

        // when - create request from DTO logic
        ParticipationRequest request = mapper.toParticipationRequest(event, userId);

        // then - map back to DTO
        ParticipationRequestDto dto = mapper.toParticipationRequestDto(request);

        // verify
        assertThat(dto.event()).isEqualTo(event.getId());
        assertThat(dto.requester()).isEqualTo(userId);
        assertThat(dto.status()).isEqualTo(RequestStatus.PENDING);
        assertThat(dto.created()).isNotNull();
    }

    private Event createEvent() {
        return eventRepository.save(Event.builder()
                .annotation("Annotation")
                .category(category)
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(Location.builder().lat(55.75f).lon(37.61f).build())
                .paid(false)
                .participantLimit(100)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .title("Event")
                .initiator(initiator)
                .build());
    }
}