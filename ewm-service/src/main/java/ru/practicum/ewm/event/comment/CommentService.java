package ru.practicum.ewm.event.comment;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.validation.Validation;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final EventRepository eventRepository;
    private final Validation validation;
    private final CommentRepository commentRepository;

    public CommentDto createComment(Long userId, Long eventId, NewCommentRequest request) {

        Event targetEvent = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Событие не найдено"));

        validation.dataEndEventValidation(targetEvent.getEventDate());
        validation.userFromCommentValidation(userId, eventId);
        validation.commentUserExistValidation(userId, eventId);






    }
}
