package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.CompilationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link CompilationSession} entities.
 * Provides standard CRUD operations plus chronological listing.
 */
@Repository
public interface CompilationSessionRepository extends JpaRepository<CompilationSession, Long> {

    /**
     * Returns all sessions sorted by creation date descending (most recent first).
     *
     * @return list of all {@link CompilationSession} entities
     */
    List<CompilationSession> findAllByOrderByCreatedAtDesc();
}