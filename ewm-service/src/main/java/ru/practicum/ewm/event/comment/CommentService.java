package ru.practicum.ewm.event.comment;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.validation.Validation;

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

        validation.dataEndEventValidation(event.getEventDate());
        validation.userFromCommentValidation(userId, eventId);
        validation.commentUserExistValidation(userId, eventId);

        Comment newComment = commentMapper.mapToComment(author, event, request);
        return commentMapper.mapToCommentDto(commentRepository.save(newComment));
    }
}
