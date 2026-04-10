package ru.practicum.ewm.event.comment;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.validation.Validation;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class CommentController {

    private final Validation validation;
    private final CommentService commentService;

    @PostMapping("/{userId}/events/{eventId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentRequest request) {
        log.info("Комментарий: запрос на создание {}", request);

        CommentDto comment = commentService.createComment(userId, eventId, request);
        log.info("Комментарий создан с id={}", comment.getId());
        return comment;
    }

    @PatchMapping("/{userId}/events/{eventId}/comment")
    public CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateCommentRequest request) {
        log.info("Комментарий: запрос на обновление {}", request);

        validation.userIdValidation(userId);
        validation.eventIdValidation(eventId);

        CommentDto updatedComment = commentService.updateComment(userId, eventId, request);
        log.info("Комментарий обновлен с id={}", updatedComment.getId());
        return updatedComment;
    }

    @GetMapping("/{userId}/comments")
    public List<CommentDto> getAllUserComments(@PathVariable Long userId) {
        log.info("Комментарий: запрос на получение всех комментариев пользователя");
        validation.userIdValidation(userId);
        return commentService.getAllUserComments(userId);

    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getAllEventComments(@PathVariable Long eventId) {
        log.info("Комментарий: запрос на получение всех комментариев события");
        validation.eventIdValidation(eventId);
        return commentService.getAllEventComments(eventId);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("Комментарий: запрос на удаление {}", commentId);
        commentService.delete(commentId);
        log.info("Комментарий {} удален", commentId);

    }

}
