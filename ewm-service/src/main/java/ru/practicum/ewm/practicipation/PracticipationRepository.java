package ru.practicum.ewm.practicipation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface PracticipationRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByEventIdAndRequesterId(Long eventId, Long userId);
}
