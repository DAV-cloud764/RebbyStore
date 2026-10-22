package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.AuditActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface AuditActivityLogRepository
        extends JpaRepository<AuditActivityLog, Long> {

    @Query(
            value = """
            SELECT a
            FROM AuditActivityLog a
            WHERE a.createdAt >= :start
              AND a.createdAt < :end
              AND (
                  :moduleName IS NULL
                  OR a.moduleName = :moduleName
              )
              AND (
                  :outcome IS NULL
                  OR a.outcome = :outcome
              )
              AND (
                  :actorUsername IS NULL
                  OR LOWER(COALESCE(a.actorUsername, ''))
                     LIKE CONCAT('%', LOWER(CAST(:actorUsername AS String)), '%')
              )
              AND (
                  :search IS NULL
                  OR LOWER(a.requestPath)
                     LIKE CONCAT('%', LOWER(CAST(:search AS String)), '%')
                  OR LOWER(a.action)
                     LIKE CONCAT('%', LOWER(CAST(:search AS String)), '%')
              )
            ORDER BY a.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(a)
            FROM AuditActivityLog a
            WHERE a.createdAt >= :start
              AND a.createdAt < :end
              AND (
                  :moduleName IS NULL
                  OR a.moduleName = :moduleName
              )
              AND (
                  :outcome IS NULL
                  OR a.outcome = :outcome
              )
              AND (
                  :actorUsername IS NULL
                  OR LOWER(COALESCE(a.actorUsername, ''))
                     LIKE LOWER(CONCAT('%', :actorUsername, '%'))
              )
              AND (
                  :search IS NULL
                  OR LOWER(a.requestPath)
                     LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(a.action)
                     LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """
    )
    Page<AuditActivityLog> searchActivityLogs(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("moduleName") String moduleName,
            @Param("outcome") String outcome,
            @Param("actorUsername") String actorUsername,
            @Param("search") String search,
            Pageable pageable
    );

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            OffsetDateTime start,
            OffsetDateTime end
    );

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndOutcome(
            OffsetDateTime start,
            OffsetDateTime end,
            String outcome
    );

    @Query("""
    SELECT a.moduleName, COUNT(a)
    FROM AuditActivityLog a
    WHERE a.createdAt >= :start
      AND a.createdAt < :end
    GROUP BY a.moduleName
    ORDER BY a.moduleName
    """)
    List<Object[]> countActivitiesByModuleBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}