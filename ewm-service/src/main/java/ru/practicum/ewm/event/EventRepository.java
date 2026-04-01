package ru.practicum.ewm.event;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "LEFT JOIN FETCH e.location " +
            "WHERE e.id = :id")
    Optional<Event> findByIdWithDetails(Long id);

}
