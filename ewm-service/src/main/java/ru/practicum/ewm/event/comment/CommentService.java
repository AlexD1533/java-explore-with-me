package ru.practicum.ewm.event.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.User;
import ru.practicum.ewm.UserRepository;
import ru.practicum.ewm.validation.Validation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final EventRepository eventRepository;
    private final Validation validation;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;

    public CommentDto createComment(Long userId, Long eventId, NewCommentRequest request) {

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие не найдено"));

        User author = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));

        validation.noPublicEventValidationForComment(event);
        validation.userFromCommentValidation(userId, eventId);
        validation.commentUserExistValidation(userId, eventId);

        Comment newComment = commentMapper.mapToComment(author, event, request);
        return commentMapper.mapToCommentDto(commentRepository.save(newComment));
    }

    public CommentDto updateComment(Long userId, Long eventId, UpdateCommentRequest request) {

        Comment comment = commentRepository.findByAuthorIdAndEventId(userId, eventId).orElseThrow(() ->
                new NotFoundException("Комментарий не найден"));

        commentMapper.mapFromUpdateComment(comment, request);
        commentRepository.save(comment);
        return commentMapper.mapToCommentDto(comment);
    }

    public List<CommentDto> getAllUserComments(Long userId) {

        List<Comment> comments = commentRepository.findAllByAuthorId(userId);
        return comments.stream().map(commentMapper::mapToCommentDto).toList();
    }

    public List<CommentDto> getAllEventComments(Long eventId) {
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        return commentMapper.mapToCommentDto(comments);
    }

    public void delete(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
