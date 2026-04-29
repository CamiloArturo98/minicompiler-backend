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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {

    private final CompilationRecordRepository recordRepository;
    private final CompilationSessionRepository sessionRepository;

    public PageResponse<RecordResponse> findAll(int page, int size) {
        Page<CompilationRecord> result = recordRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast()
        );
    }

    public RecordResponse findById(Long id) {
        return recordRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Record", id));
    }

    public List<RecordResponse> findBySession(Long sessionId) {
        return recordRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
                .stream().map(this::toResponse).toList();
    }

    public List<RecordResponse> findBySuccess(boolean success) {
        return recordRepository.findBySuccessOrderByCreatedAtDesc(success)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public RecordResponse save(Long sessionId, CompilationRecord record) {
        CompilationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));
        record.setSession(session);
        return toResponse(recordRepository.save(record));
    }

    @Transactional
    public void delete(Long id) {
        if (!recordRepository.existsById(id)) throw new ResourceNotFoundException("Record", id);
        recordRepository.deleteById(id);
    }

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