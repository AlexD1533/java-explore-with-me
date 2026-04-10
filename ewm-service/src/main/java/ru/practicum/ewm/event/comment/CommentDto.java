package ru.practicum.ewm.event.comment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {

    private Long id;
    private String text;
    private String authorName;
    private LocalDateTime created;
}