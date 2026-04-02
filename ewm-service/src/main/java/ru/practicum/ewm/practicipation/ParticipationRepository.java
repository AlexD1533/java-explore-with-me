package ru.practicum.ewm.practicipation;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
@Repository
public interface ParticipationRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByEventIdAndRequesterId(Long eventId, Long userId);


    @Query("SELECT p.id AS id, p.status AS status, e.requestModeration, e.participantLimit AS participantLimit " +
            "FROM ParticipationRequest AS p " +
            "JOIN p.event AS e " +
            "WHERE e.initiator.id = :userId AND e.id = :eventId " +
            "AND (p.id IN :ids) " +
            "AND (p.status = 'PENDING')" )
    List<ViewRequest> findAllForUpdateByParam(@Param("ids") List<Long> ids,
                                              @Param("eventId") Long eventId,
                                              @Param("userId") Long userId);

@Query("SELECT COUNT(r) FROM ParticipationRequest AS r " +
        "JOIN r.event AS e " +
        "WHERE e.id = :eventId AND (r.status = 'CONFIRMED' OR r.status = 'PENDING')")
    Long countConfirmedRequests(@Param("eventId")Long eventId);


    Integer countByEventId(Long eventId);

    List<ParticipationRequest> findAllByRequesterId(@NotNull Long userId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long userId);
}
