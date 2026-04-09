package ru.practicum.ewm.practicipation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByEventIdAndRequesterId(Long eventId, Long userId);


    @Query("SELECT p FROM ParticipationRequest p " +
            "JOIN FETCH p.event e " +
            "JOIN FETCH e.location " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.initiator.id = :userId " +
            "AND e.id = :eventId " +
            "AND p.id IN :ids ")
    List<ParticipationRequest> findAllForUpdateByParam(@Param("ids") List<Long> ids,
                                                       @Param("eventId") Long eventId,
                                                       @Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM ParticipationRequest AS r " +
            "JOIN r.event AS e " +
            "WHERE e.id = :eventId AND (r.status = 'CONFIRMED')")
    Long countConfirmedRequests(@Param("eventId") Long eventId);


    Integer countByEventId(Long eventId);


    List<ParticipationRequest> findAllByRequesterId(Long userId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long userId);


    @Query("SELECT r FROM ParticipationRequest r " +
            "JOIN r.event AS e " +
            "JOIN e.initiator AS i " +
            "WHERE e.id = :eventId AND i.id = :userId")
    List<ParticipationRequest> findAllByEventIdAndInitiatorId(@Param("eventId") Long eventId,
                                                              @Param("userId") Long userId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long userId);

    @Query("SELECT r FROM ParticipationRequest AS r " +
            "JOIN r.event AS e " +
            "WHERE :eventIds IS NULL OR e.id IN :eventIds " +
            "AND (r.status = 'CONFIRMED')")
    List<ParticipationRequest> findAllByEventIdsConfirmed(@Param("eventIds") List<Long> eventIds);

    Integer countByEventIdAndStatus(Long id, RequestStatus requestStatus);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long id, RequestStatus requestStatus);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);
}
