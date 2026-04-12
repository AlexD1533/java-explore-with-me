package ru.practicum.ewm.event.comment;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.User;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {


    public Comment mapToComment(User author, Event event, NewCommentRequest request) {
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    public CommentDto mapToCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());

        if (comment.getAuthor() != null) {
            dto.setAuthorName(comment.getAuthor().getName());
        }

        dto.setCreated(comment.getCreated());
        return dto;
    }

    public List<CommentDto> mapToCommentDto(List<Comment> comments) {
        return comments.stream()
                .map(this::mapToCommentDto)
                .collect(Collectors.toList());
    }

    public void mapFromUpdateComment(Comment comment, UpdateCommentRequest request) {

        if (!request.getText().isBlank()) {
            comment.setText(request.getText());
        }
    }
}