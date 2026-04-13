package ru.practicum.ewm.event.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.user.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CommentMapper.class)
@DisplayName("Integration tests for CommentMapper")
class CommentMapperIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentMapper commentMapper;

    private User testUser;
    private Event testEvent;
    private Category testCategory;
    private Location testLocation;
    private User testInitiator;

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
    @DisplayName("Should map NewCommentRequest to Comment")
    void shouldMapNewCommentRequestToComment() {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Test comment text");

        Comment result = commentMapper.mapToComment(testUser, testEvent, request);

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Test comment text");
        assertThat(result.getAuthor()).isEqualTo(testUser);
        assertThat(result.getEvent()).isEqualTo(testEvent);
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should map Comment to CommentDto")
    void shouldMapCommentToCommentDto() {
        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setAuthor(testUser);
        comment.setEvent(testEvent);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = entityManager.persist(comment);
        entityManager.flush();

        CommentDto result = commentMapper.mapToCommentDto(savedComment);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedComment.getId());
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthorName()).isEqualTo(testUser.getName());
        assertThat(result.getCreated()).isEqualTo(savedComment.getCreated());
    }


    @Test
    @DisplayName("Should map list of Comments to list of CommentDtos")
    void shouldMapListOfCommentsToListOfCommentDtos() {
        Comment comment1 = new Comment();
        comment1.setText("First comment");
        comment1.setAuthor(testUser);
        comment1.setEvent(testEvent);
        comment1.setCreated(LocalDateTime.now());
        Comment savedComment1 = entityManager.persist(comment1);

        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        entityManager.persist(anotherUser);

        Comment comment2 = new Comment();
        comment2.setText("Second comment");
        comment2.setAuthor(anotherUser);
        comment2.setEvent(testEvent);
        comment2.setCreated(LocalDateTime.now());
        Comment savedComment2 = entityManager.persist(comment2);
        entityManager.flush();

        List<Comment> comments = Arrays.asList(savedComment1, savedComment2);

        List<CommentDto> result = commentMapper.mapToCommentDto(comments);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getText()).isEqualTo("First comment");
        assertThat(result.get(0).getAuthorName()).isEqualTo(testUser.getName());
        assertThat(result.get(1).getText()).isEqualTo("Second comment");
        assertThat(result.get(1).getAuthorName()).isEqualTo("Another User");
    }

    @Test
    @DisplayName("Should map empty list to empty list")
    void shouldMapEmptyListToEmptyList() {
        List<CommentDto> result = commentMapper.mapToCommentDto(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should update comment from UpdateCommentRequest with non-blank text")
    void shouldUpdateCommentFromUpdateCommentRequestWithNonBlankText() {
        Comment comment = new Comment();
        comment.setText("Original text");
        comment.setAuthor(testUser);
        comment.setEvent(testEvent);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = entityManager.persist(comment);
        entityManager.flush();

        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setText("Updated text");

        commentMapper.mapFromUpdateComment(savedComment, request);
        entityManager.flush();

        Comment updatedComment = entityManager.find(Comment.class, savedComment.getId());
        assertThat(updatedComment.getText()).isEqualTo("Updated text");
    }

    @Test
    @DisplayName("Should not update comment when text is blank")
    void shouldNotUpdateCommentWhenTextIsBlank() {
        Comment comment = new Comment();
        comment.setText("Original text");
        comment.setAuthor(testUser);
        comment.setEvent(testEvent);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = entityManager.persist(comment);
        entityManager.flush();

        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setText("   ");

        commentMapper.mapFromUpdateComment(savedComment, request);
        entityManager.flush();

        Comment updatedComment = entityManager.find(Comment.class, savedComment.getId());
        assertThat(updatedComment.getText()).isEqualTo("Original text");
    }

}