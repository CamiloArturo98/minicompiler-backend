package com.minicompiler.service;

import com.minicompiler.domain.entity.CompilationSession;
import com.minicompiler.domain.repository.CompilationSessionRepository;
import com.minicompiler.dto.request.SessionRequest;
import com.minicompiler.dto.response.SessionResponse;
import com.minicompiler.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing persistence and retrieval of {@link CompilationSession} entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final CompilationSessionRepository repository;

    // =========================================================================
    // Public API
    // =========================================================================

    /** Returns all sessions ordered by creation date descending. */
    public List<SessionResponse> findAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns a single session by its ID.
     *
     * @param  id the session ID
     * @throws ResourceNotFoundException if no session exists with the given ID
     */
    public SessionResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));
    }

    /**
     * Creates and persists a new session.
     *
     * @param  request the validated session request body
     * @return the created session response
     */
    @Transactional
    public SessionResponse create(SessionRequest request) {
        var session = CompilationSession.builder()
                .name(request.name())
                .description(request.description())
                .build();
        var saved = repository.save(session);
        log.info("Session created: id={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    /**
     * Updates an existing session's name and description.
     *
     * @param  id      the session ID to update
     * @param  request the validated update request body
     * @throws ResourceNotFoundException if no session exists with the given ID
     */
    @Transactional
    public SessionResponse update(Long id, SessionRequest request) {
        var session = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));
        session.setName(request.name());
        session.setDescription(request.description());
        return toResponse(repository.save(session));
    }

    /**
     * Deletes a session by its ID.
     *
     * @param  id the session ID to delete
     * @throws ResourceNotFoundException if no session exists with the given ID
     */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Session", id);
        repository.deleteById(id);
        log.info("Session deleted: id={}", id);
    }

    // =========================================================================
    // Mapper
    // =========================================================================

    private SessionResponse toResponse(CompilationSession s) {
        return new SessionResponse(
                s.getId(), s.getName(), s.getDescription(),
                s.getCreatedAt(), s.getRecords().size()
        );
    }
}