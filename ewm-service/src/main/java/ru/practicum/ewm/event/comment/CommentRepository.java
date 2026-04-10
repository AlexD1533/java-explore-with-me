package ru.practicum.ewm.event.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    boolean existsByAuthorIdAndEventId(Long userId, Long eventId);

    List<Comment> findAllByAuthorId(Long userId);

    List<Comment> findAllByEventId(Long eventId);

    Optional<Comment> findByAuthorIdAndEventId(Long userId, Long eventId);
}
