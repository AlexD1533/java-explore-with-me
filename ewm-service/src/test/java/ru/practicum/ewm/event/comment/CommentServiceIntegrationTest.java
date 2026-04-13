package ru.practicum.ewm.event.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.practicipation.ParticipationRepository;
import ru.practicum.ewm.practicipation.RequestStatus;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.validation.Validation;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({CommentService.class, CommentMapper.class, Validation.class})
@DisplayName("Integration tests for CommentService")
class CommentServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ParticipationRepository participationRepository;

    private User testUser;
    private User testInitiator;
    private Event testEvent;
    private Category testCategory;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Test Category");
        entityManager.persist(testCategory);

        testLocation = new Location();
        testLocation.setLat(55.7558f);
        testLocation.setLon(37.6173f);
        entityManager.persist(testLocation);

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        testInitiator = new User();
        testInitiator.setName("Initiator");
        testInitiator.setEmail("initiator@example.com");
        entityManager.persist(testInitiator);

        testEvent = new Event();
        testEvent.setAnnotation("Test annotation");
        testEvent.setDescription("Test description");
        testEvent.setTitle("Test Event");
        testEvent.setEventDate(LocalDateTime.now().plusDays(1));
        testEvent.setCreatedOn(LocalDateTime.now());
        testEvent.setPublishedOn(LocalDateTime.now());
        testEvent.setState(EventState.PUBLISHED);
        testEvent.setPaid(false);
        testEvent.setParticipantLimit(100);
        testEvent.setRequestModeration(false);
        testEvent.setConfirmedRequests(0L);
        testEvent.setViews(0L);
        testEvent.setCategory(testCategory);
        testEvent.setInitiator(testInitiator);
        testEvent.setLocation(testLocation);
        entityManager.persist(testEvent);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should create comment successfully")
    void shouldCreateCommentSuccessfully() {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Test comment text");

        when(participationRepository.existsByRequesterIdAndEventIdAndStatus(
                testUser.getId(), testEvent.getId(), RequestStatus.CONFIRMED))
                .thenReturn(true);

        CommentDto result = commentService.createComment(testUser.getId(), testEvent.getId(), request);

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Test comment text");
        assertThat(result.getAuthorName()).isEqualTo(testUser.getName());
        assertThat(result.getCreated()).isNotNull();

        List<Comment> savedComments = commentRepository.findAll();
        assertThat(savedComments).hasSize(1);
        assertThat(savedComments.get(0).getText()).isEqualTo("Test comment text");
    }

    @Test
    @DisplayName("Should throw NotFoundException when event not found during creation")
    void shouldThrowNotFoundExceptionWhenEventNotFound() {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Test comment");

        assertThatThrownBy(() -> commentService.createComment(testUser.getId(), 999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Событие не найдено");
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found during creation")
    void shouldThrowNotFoundExceptionWhenUserNotFound() {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Test comment");

        assertThatThrownBy(() -> commentService.createComment(999L, testEvent.getId(), request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Should throw ConflictException when event is not published")
    void shouldThrowConflictExceptionWhenEventNotPublished() {
        Event pendingEvent = new Event();
        pendingEvent.setAnnotation("Pending annotation");
        pendingEvent.setDescription("Pending description");
        pendingEvent.setTitle("Pending Event");
        pendingEvent.setEventDate(LocalDateTime.now().plusDays(1));
        pendingEvent.setCreatedOn(LocalDateTime.now());
        pendingEvent.setState(EventState.PENDING);
        pendingEvent.setPaid(false);
        pendingEvent.setParticipantLimit(100);
        pendingEvent.setRequestModeration(false);
        pendingEvent.setCategory(testCategory);
        pendingEvent.setInitiator(testInitiator);
        pendingEvent.setLocation(testLocation);
        entityManager.persist(pendingEvent);
        entityManager.flush();

        NewCommentRequest request = new NewCommentRequest();
        request.setText("Test comment");

        assertThatThrownBy(() -> commentService.createComment(testUser.getId(), pendingEvent.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Нельзя комментировать неопубликованное событие");
    }

    @Test
    @DisplayName("Should throw ConflictException when user has no confirmed participation")
    void shouldThrowConflictExceptionWhenNoConfirmedParticipation() {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Test comment");

        when(participationRepository.existsByRequesterIdAndEventIdAndStatus(
                testUser.getId(), testEvent.getId(), RequestStatus.CONFIRMED))
                .thenReturn(false);

        assertThatThrownBy(() -> commentService.createComment(testUser.getId(), testEvent.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Нет подтвержденных заявок");
    }

    @Test
    @DisplayName("Should throw ConflictException when user already commented")
    void shouldThrowConflictExceptionWhenUserAlreadyCommented() {
        Comment existingComment = new Comment();
        existingComment.setText("Existing comment");
        existingComment.setAuthor(testUser);
        existingComment.setEvent(testEvent);
        existingComment.setCreated(LocalDateTime.now());
        entityManager.persist(existingComment);
        entityManager.flush();

        NewCommentRequest request = new NewCommentRequest();
        request.setText("New comment");

        when(participationRepository.existsByRequesterIdAndEventIdAndStatus(
                testUser.getId(), testEvent.getId(), RequestStatus.CONFIRMED))
                .thenReturn(true);

        assertThatThrownBy(() -> commentService.createComment(testUser.getId(), testEvent.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Пользователь уже оставлял комментарий");
    }

    @Test
    @DisplayName("Should update comment successfully")
    void shouldUpdateCommentSuccessfully() {
        Comment existingComment = new Comment();
        existingComment.setText("Original text");
        existingComment.setAuthor(testUser);
        existingComment.setEvent(testEvent);
        existingComment.setCreated(LocalDateTime.now());
        entityManager.persist(existingComment);
        entityManager.flush();

        UpdateCommentRequest updateRequest = new UpdateCommentRequest();
        updateRequest.setText("Updated text");

        CommentDto result = commentService.updateComment(testUser.getId(), testEvent.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Updated text");

        Comment updatedComment = entityManager.find(Comment.class, existingComment.getId());
        assertThat(updatedComment.getText()).isEqualTo("Updated text");
    }

    @Test
    @DisplayName("Should throw NotFoundException when comment not found during update")
    void shouldThrowNotFoundExceptionWhenCommentNotFoundForUpdate() {
        UpdateCommentRequest updateRequest = new UpdateCommentRequest();
        updateRequest.setText("Updated text");

        assertThatThrownBy(() -> commentService.updateComment(999L, 999L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Комментарий не найден");
    }

    @Test
    @DisplayName("Should get all user comments")
    void shouldGetAllUserComments() {
        Comment comment1 = new Comment();
        comment1.setText("Comment 1");
        comment1.setAuthor(testUser);
        comment1.setEvent(testEvent);
        comment1.setCreated(LocalDateTime.now());
        entityManager.persist(comment1);

        Event anotherEvent = new Event();
        anotherEvent.setAnnotation("Another annotation");
        anotherEvent.setDescription("Another description");
        anotherEvent.setTitle("Another Event");
        anotherEvent.setEventDate(LocalDateTime.now().plusDays(2));
        anotherEvent.setCreatedOn(LocalDateTime.now());
        anotherEvent.setState(EventState.PUBLISHED);
        anotherEvent.setPaid(false);
        anotherEvent.setParticipantLimit(50);
        anotherEvent.setRequestModeration(false);
        anotherEvent.setCategory(testCategory);
        anotherEvent.setInitiator(testInitiator);
        anotherEvent.setLocation(testLocation);
        entityManager.persist(anotherEvent);

        Comment comment2 = new Comment();
        comment2.setText("Comment 2");
        comment2.setAuthor(testUser);
        comment2.setEvent(anotherEvent);
        comment2.setCreated(LocalDateTime.now());
        entityManager.persist(comment2);
        entityManager.flush();

        List<CommentDto> result = commentService.getAllUserComments(testUser.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CommentDto::getText)
                .containsExactlyInAnyOrder("Comment 1", "Comment 2");
    }

    @Test
    @DisplayName("Should return empty list when user has no comments")
    void shouldReturnEmptyListWhenUserHasNoComments() {
        List<CommentDto> result = commentService.getAllUserComments(testUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get all event comments")
    void shouldGetAllEventComments() {
        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        entityManager.persist(anotherUser);

        Comment comment1 = new Comment();
        comment1.setText("Comment from user 1");
        comment1.setAuthor(testUser);
        comment1.setEvent(testEvent);
        comment1.setCreated(LocalDateTime.now());
        entityManager.persist(comment1);

        Comment comment2 = new Comment();
        comment2.setText("Comment from user 2");
        comment2.setAuthor(anotherUser);
        comment2.setEvent(testEvent);
        comment2.setCreated(LocalDateTime.now());
        entityManager.persist(comment2);
        entityManager.flush();

        List<CommentDto> result = commentService.getAllEventComments(testEvent.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CommentDto::getText)
                .containsExactlyInAnyOrder("Comment from user 1", "Comment from user 2");
    }

    @Test
    @DisplayName("Should return empty list when event has no comments")
    void shouldReturnEmptyListWhenEventHasNoComments() {
        List<CommentDto> result = commentService.getAllEventComments(testEvent.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should delete comment successfully")
    void shouldDeleteCommentSuccessfully() {
        Comment comment = new Comment();
        comment.setText("To be deleted");
        comment.setAuthor(testUser);
        comment.setEvent(testEvent);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = entityManager.persist(comment);
        entityManager.flush();

        assertThat(commentRepository.findById(savedComment.getId())).isPresent();

        commentService.delete(savedComment.getId());

        assertThat(commentRepository.findById(savedComment.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should handle delete when comment does not exist")
    void shouldHandleDeleteWhenCommentDoesNotExist() {
        commentService.delete(999L);
    }
}