package ru.practicum.ewm.event.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    boolean existByUserIdAndEventId(Long userId, Long eventId);
}
