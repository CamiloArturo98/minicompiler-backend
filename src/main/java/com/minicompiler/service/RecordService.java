package com.minicompiler.service;

import com.minicompiler.domain.entity.CompilationRecord;
import com.minicompiler.domain.entity.CompilationSession;
import com.minicompiler.domain.repository.CompilationRecordRepository;
import com.minicompiler.domain.repository.CompilationSessionRepository;
import com.minicompiler.dto.response.PageResponse;
import com.minicompiler.dto.response.RecordResponse;
import com.minicompiler.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing persistence and retrieval of {@link CompilationRecord} entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {

    private final CompilationRecordRepository  recordRepository;
    private final CompilationSessionRepository sessionRepository;

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Returns a paginated list of all records ordered by creation date descending.
     *
     * @param page zero-based page index
     * @param size number of records per page
     */
    public PageResponse<RecordResponse> findAll(int page, int size) {
        var result = recordRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast()
        );
    }

    /**
     * Returns a single record by its ID.
     *
     * @param  id the record ID
     * @throws ResourceNotFoundException if no record exists with the given ID
     */
    public RecordResponse findById(Long id) {
        return recordRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Record", id));
    }

    /**
     * Returns all records belonging to the given session, most recent first.
     *
     * @param sessionId the parent session ID
     */
    public List<RecordResponse> findBySession(Long sessionId) {
        return recordRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Returns all records matching the given success flag, most recent first.
     *
     * @param success {@code true} for successful compilations, {@code false} for failed ones
     */
    public List<RecordResponse> findBySuccess(boolean success) {
        return recordRepository.findBySuccessOrderByCreatedAtDesc(success)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Persists a new compilation record linked to the given session.
     *
     * @param  sessionId the ID of the parent session
     * @param  record    the record entity to persist
     * @throws ResourceNotFoundException if no session exists with the given ID
     */
    @Transactional
    public RecordResponse save(Long sessionId, CompilationRecord record) {
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));
        record.setSession(session);
        return toResponse(recordRepository.save(record));
    }

    /**
     * Deletes a record by its ID.
     *
     * @param  id the record ID to delete
     * @throws ResourceNotFoundException if no record exists with the given ID
     */
    @Transactional
    public void delete(Long id) {
        if (!recordRepository.existsById(id)) throw new ResourceNotFoundException("Record", id);
        recordRepository.deleteById(id);
    }

    /**
     * Persists a standalone compilation record not linked to any session.
     * Used by the compiler pipeline to automatically log every compilation attempt.
     *
     * @param  record the populated record entity to persist
     * @return the saved record as a response DTO
     */
    @Transactional
    public RecordResponse saveRecord(CompilationRecord record) {
        return toResponse(recordRepository.save(record));
    }

    // =========================================================================
    // Mapper
    // =========================================================================

    private RecordResponse toResponse(CompilationRecord r) {
        return new RecordResponse(
                r.getId(),
                r.getSession() != null ? r.getSession().getId() : null,
                r.getSourceCode(), r.getOutput(), r.getBytecode(),
                r.isSuccess(), r.getErrorMessage(),
                r.getCompilationTimeMs(), r.getInstructionsExecuted(),
                r.isOptimized(), r.getCreatedAt()
        );
    }
}