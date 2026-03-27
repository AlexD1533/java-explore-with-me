package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.EndpointHit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {


    @Query("SELECT r.app AS app, r.uri AS uri, COUNT(r.ip) AS hits " +
            "FROM EndpointHit r " +
            "WHERE r.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR r.uri IN :uris) " +
            "GROUP BY r.app, r.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> getVisitsStatistic(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("uris") List<String> uris);

    @Query("SELECT r.app AS app, r.uri AS uri, COUNT(DISTINCT r.ip) AS hits " +
            "FROM EndpointHit r " +
            "WHERE r.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR r.uri IN :uris) " +
            "GROUP BY r.app, r.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> getVisitsStatisticDistinct(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("uris") List<String> uris);
}
