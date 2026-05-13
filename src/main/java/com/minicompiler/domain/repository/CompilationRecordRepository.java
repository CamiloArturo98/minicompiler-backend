package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.CompilationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link CompilationRecord} entities.
 * Provides standard CRUD operations plus filtered queries and aggregated statistics.
 */
@Repository
public interface CompilationRecordRepository extends JpaRepository<CompilationRecord, Long> {

    /**
     * Returns all records belonging to a session, most recent first.
     *
     * @param sessionId the ID of the parent session
     */
    List<CompilationRecord> findBySessionIdOrderByCreatedAtDesc(Long sessionId);

    /**
     * Returns a paginated list of all records, most recent first.
     *
     * @param pageable pagination parameters
     */
    Page<CompilationRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Returns all records matching the given success flag, most recent first.
     *
     * @param success {@code true} for successful compilations, {@code false} for failed ones
     */
    List<CompilationRecord> findBySuccessOrderByCreatedAtDesc(boolean success);

    /** Returns the total number of successful compilations. */
    @Query("SELECT COUNT(r) FROM CompilationRecord r WHERE r.success = true")
    long countSuccessful();

    /** Returns the total number of failed compilations. */
    @Query("SELECT COUNT(r) FROM CompilationRecord r WHERE r.success = false")
    long countFailed();

    /** Returns the average compilation time in milliseconds across all records. */
    @Query("SELECT AVG(r.compilationTimeMs) FROM CompilationRecord r")
    Double avgCompilationTime();
}