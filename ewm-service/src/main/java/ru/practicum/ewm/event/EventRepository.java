package ru.practicum.ewm.event;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "LEFT JOIN FETCH e.location " +
            "WHERE e.id = :eventId AND e.initiator.id = :userId")
    Optional<Event> findEventForUpdate(Long eventId, Long userId);



    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.category " +
            "WHERE (:usersIds IS NULL OR e.initiator.id IN :usersIds) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categoryIds IS NULL OR e.category.id IN :categoryIds) " +
            "AND (CAST(:rangeStart AS timestamp) IS NULL OR e.eventDate > :rangeStart) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.eventDate < :rangeEnd)")
    List<Event> findAllEventsByParam(
            @Param("usersIds") List<Long> usersIds,
            @Param("states") List<String> states,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);



    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:text IS NULL OR " +
            "(LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (CAST(:rangeStart AS timestamp) IS NULL OR e.eventDate > :rangeStart) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.eventDate < :rangeEnd)")
    List<Event> findAllEventsByParamPublic(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    Optional<Event> findByIdAndState(Long eventId, EventState state);
}