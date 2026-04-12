package ru.practicum.ewm.event.comment;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.User;


import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    Long id;
    @Column(name = "content")
    String text;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User author;
    @Column(name = "create_date")
    LocalDateTime created;
}
