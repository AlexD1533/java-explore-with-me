package ru.practicum.ewm.event.comment;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.validation.Validation;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class CommentController {

    private final Validation validation;
    private final CommentService commentService;

    @PostMapping("/{userId}/events/{eventId}/comment")
    public CommentDto createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentRequest request) {
        log.info("Комментарий: запрос на создание {}", request);

        CommentDto comment = commentService.createComment(userId, eventId, request);
        log.info("Комментарий создан с id={}", comment.getId());
        return comment;

    }


}
