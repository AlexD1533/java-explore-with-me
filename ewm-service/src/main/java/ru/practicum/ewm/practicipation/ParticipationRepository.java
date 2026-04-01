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


    @Query("SELECT p.id AS id, p.status AS status, e.requestModeration, e.participantLimit AS participantLimit " +
            "FROM ParticipationRequest AS p " +
            "JOIN p.event AS e " +
            "WHERE e.initiator.id = :userId AND e.id = :eventId " +
            "AND (p.id IN :ids)" )
    List<ViewRequest> findAllForUpdateByParam(@Param("ids") List<Integer> ids,
                                              @Param("eventId") Long eventId,
                                              @Param("userId") Long userId);

    Integer countByEventId(Long eventId);
}
