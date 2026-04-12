package ru.practicum.ewm.participation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.practicipation.ParticipationRepository;
import ru.practicum.ewm.practicipation.ParticipationRequest;
import ru.practicum.ewm.practicipation.ParticipationService;
import ru.practicum.ewm.practicipation.RequestStatus;
import ru.practicum.ewm.practicipation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.practicipation.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.practicipation.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class ParticipationServiceIntegrationTest {

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User initiator;
    private User requester;
    private Category category;
    private Event publishedEvent;
    private Event eventWithoutModeration;
    private Event eventModerationTrue;
    private Event eventWithZeroLimit;

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

        publishedEvent = createEvent(EventState.PUBLISHED, true, 100);
        eventWithoutModeration = createEvent(EventState.PUBLISHED, false, 100);
        eventModerationTrue = createEvent(EventState.PUBLISHED, true, 100);
        eventWithZeroLimit = createEvent(EventState.PUBLISHED, true, 0);
    }

    @Test
    void createRequestEvent_shouldCreatePendingRequest() {
        // when
        ParticipationRequestDto result = participationService.createRequestEvent(requester.getId(), publishedEvent.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(RequestStatus.PENDING);
        assertThat(result.event()).isEqualTo(publishedEvent.getId());
        assertThat(result.requester()).isEqualTo(requester.getId());
        assertThat(result.created()).isNotNull();
    }

    @Test
    void createRequestEvent_withoutModeration_shouldAutoConfirm() {
        // when
        ParticipationRequestDto result = participationService.createRequestEvent(requester.getId(), eventWithoutModeration.getId());

        // then
        assertThat(result.status()).isEqualTo(RequestStatus.CONFIRMED);
    }

    @Test
    void createRequestEvent_withZeroLimit_shouldAutoConfirm() {
        // when
        ParticipationRequestDto result = participationService.createRequestEvent(requester.getId(), eventWithZeroLimit.getId());

        // then
        assertThat(result.status()).isEqualTo(RequestStatus.CONFIRMED);
    }

    @Test
    void createRequestEvent_shouldThrowWhenDuplicateRequest() {
        // given
        participationService.createRequestEvent(requester.getId(), publishedEvent.getId());

        // when & then
        assertThrows(ConflictException.class, () ->
                participationService.createRequestEvent(requester.getId(), publishedEvent.getId())
        );
    }

    @Test
    void createRequestEvent_shouldThrowWhenInitiatorRequestsOwnEvent() {
        // when & then
        assertThrows(ConflictException.class, () ->
                participationService.createRequestEvent(initiator.getId(), publishedEvent.getId())
        );
    }

    @Test
    void createRequestEvent_shouldThrowWhenEventNotPublished() {
        // given
        Event pendingEvent = createEvent(EventState.PENDING, true, 100);

        // when & then
        assertThrows(ConflictException.class, () ->
                participationService.createRequestEvent(requester.getId(), pendingEvent.getId())
        );
    }

    @Test
    void createRequestEvent_shouldThrowWhenLimitReached() {
        // given
        Event limitedEvent = createEvent(EventState.PUBLISHED, true, 1);
        User anotherRequester = userRepository.save(User.builder()
                .name("Another")
                .email("another@test.com")
                .build());

        // Fill the limit
        ParticipationRequest confirmed = ParticipationRequest.builder()
                .event(limitedEvent)
                .requester(anotherRequester)
                .status(RequestStatus.CONFIRMED)
                .build();
        participationRepository.save(confirmed);

        // when & then
        assertThrows(ConflictException.class, () ->
                participationService.createRequestEvent(requester.getId(), limitedEvent.getId())
        );
    }

    @Test
    void getParticipationByUserId_shouldReturnUserRequests() {
        // given
        ParticipationRequest request1 = createRequest(publishedEvent, requester, RequestStatus.PENDING);
        User otherUser = userRepository.save(User.builder().name("Other").email("other@test.com").build());
        Event otherEvent = createEvent(EventState.PUBLISHED, true, 100);
        createRequest(otherEvent, otherUser, RequestStatus.PENDING);

        // when
        List<ParticipationRequestDto> result = participationService.getParticipationByUserId(requester.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(request1.getId());
    }

    @Test
    void getParticipationByUserIdAndEventId_shouldReturnEventRequestsForInitiator() {
        // given
        createRequest(publishedEvent, requester, RequestStatus.PENDING);
        User anotherRequester = userRepository.save(User.builder()
                .name("Another")
                .email("another@test.com")
                .build());
        createRequest(publishedEvent, anotherRequester, RequestStatus.PENDING);

        // when
        List<ParticipationRequestDto> result = participationService
                .getParticipationByUserIdAndEventId(initiator.getId(), publishedEvent.getId());

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void cancelRequestsByUser_shouldCancelRequest() {
        // given
        ParticipationRequest request = createRequest(publishedEvent, requester, RequestStatus.PENDING);

        // when
        ParticipationRequestDto result = participationService.cancelRequestsByUser(request.getId(), requester.getId());

        // then
        assertThat(result.status()).isEqualTo(RequestStatus.CANCELED);

        ParticipationRequest updated = participationRepository.findById(request.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(RequestStatus.CANCELED);
    }

    @Test
    void cancelRequestsByUser_shouldThrowWhenRequestNotFound() {
        // when & then
        assertThrows(NotFoundException.class, () ->
                participationService.cancelRequestsByUser(999L, requester.getId())
        );
    }

    @Test
    void updateRequests_shouldConfirmPendingRequests() {
        // given
        ParticipationRequest request1 = createRequest(publishedEvent, requester, RequestStatus.PENDING);
        User anotherRequester = userRepository.save(User.builder()
                .name("Another")
                .email("another@test.com")
                .build());
        ParticipationRequest request2 = createRequest(publishedEvent, anotherRequester, RequestStatus.PENDING);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request1.getId(), request2.getId()));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        // when
        EventRequestStatusUpdateResult result = participationService
                .updateRequests(publishedEvent.getId(), initiator.getId(), updateRequest);

        // then
        assertThat(result.getConfirmedRequests()).hasSize(2);
        assertThat(result.getRejectedRequests()).isEmpty();

        List<ParticipationRequest> updated = participationRepository.findAllById(List.of(request1.getId(), request2.getId()));
        assertThat(updated).allMatch(r -> r.getStatus() == RequestStatus.CONFIRMED);
    }

    @Test
    void updateRequests_shouldRejectPendingRequests() {
        // given
        ParticipationRequest request = createRequest(publishedEvent, requester, RequestStatus.PENDING);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId()));
        updateRequest.setStatus(RequestStatus.REJECTED);

        // when
        EventRequestStatusUpdateResult result = participationService
                .updateRequests(publishedEvent.getId(), initiator.getId(), updateRequest);

        // then
        assertThat(result.getRejectedRequests()).hasSize(1);
        assertThat(result.getConfirmedRequests()).isEmpty();

        ParticipationRequest updated = participationRepository.findById(request.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }

    @Test
    void updateRequests_shouldThrowWhenRequestNotPending() {
        // given
        ParticipationRequest confirmedRequest = createRequest(publishedEvent, requester, RequestStatus.CONFIRMED);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(confirmedRequest.getId()));
        updateRequest.setStatus(RequestStatus.REJECTED);

        // when & then
        assertThrows(ConflictException.class, () ->
                participationService.updateRequests(publishedEvent.getId(), initiator.getId(), updateRequest)
        );
    }

    @Test
    void updateRequests_shouldRejectExcessWhenLimitReached() {
        // given
        Event limitedEvent = createEvent(EventState.PUBLISHED, true, 2);

        // Fill one slot через сервис (имитируем реальную работу)
        User confirmedUser = userRepository.save(User.builder().name("Confirmed").email("conf@test.com").build());

        // Создаем и подтверждаем первую заявку через сервисную логику
        ParticipationRequestDto confirmedDto = participationService.createRequestEvent(confirmedUser.getId(), limitedEvent.getId());

        // Принудительно подтверждаем (без модерации или через updateRequests)
        ParticipationRequest confirmed = participationRepository.findById(confirmedDto.id()).orElseThrow();
        confirmed.setStatus(RequestStatus.CONFIRMED);
        participationRepository.save(confirmed);

        // Two pending requests, but only 1 slot left
        User pendingUser1 = userRepository.save(User.builder().name("Pending1").email("p1@test.com").build());
        User pendingUser2 = userRepository.save(User.builder().name("Pending2").email("p2@test.com").build());

        // Создаем через сервис, чтобы получились PENDING
        ParticipationRequestDto pendingDto1 = participationService.createRequestEvent(pendingUser1.getId(), limitedEvent.getId());
        ParticipationRequestDto pendingDto2 = participationService.createRequestEvent(pendingUser2.getId(), limitedEvent.getId());

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(pendingDto1.id(), pendingDto2.id()));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        // when
        EventRequestStatusUpdateResult result = participationService
                .updateRequests(limitedEvent.getId(), initiator.getId(), updateRequest);

        // then
        assertThat(result.getConfirmedRequests()).hasSize(1);
        assertThat(result.getRejectedRequests()).hasSize(1);
    }


    @Test
    void updateRequests_shouldAutoConfirmWithoutModeration() {
        // given
        ParticipationRequest request = createRequest(eventModerationTrue, requester, RequestStatus.PENDING);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId()));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        // when
        EventRequestStatusUpdateResult result = participationService
                .updateRequests(eventModerationTrue.getId(), initiator.getId(), updateRequest);

        // then - should be confirmed even with limit check logic
        assertThat(result.getConfirmedRequests()).hasSize(1);
    }

    private Event createEvent(EventState state, boolean moderation, int limit) {
        return eventRepository.save(Event.builder()
                .annotation("Test Annotation Test Annotation")
                .category(category)
                .description("Test Description Test Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(Location.builder().lat(55.75f).lon(37.61f).build())
                .paid(false)
                .participantLimit(limit)
                .requestModeration(moderation)
                .state(state)
                .title("Test Event")
                .initiator(initiator)
                .build());
    }

    private ParticipationRequest createRequest(Event event, User requester, RequestStatus status) {
        return participationRepository.save(ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .status(status)
                .build());
    }
}