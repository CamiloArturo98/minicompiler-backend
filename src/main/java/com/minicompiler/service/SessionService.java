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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final CompilationSessionRepository repository;

    public List<SessionResponse> findAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SessionResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));
    }

    @Transactional
    public SessionResponse create(SessionRequest request) {
        CompilationSession session = CompilationSession.builder()
                .name(request.name())
                .description(request.description())
                .build();
        CompilationSession saved = repository.save(session);
        log.info("Session created: id={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public SessionResponse update(Long id, SessionRequest request) {
        CompilationSession session = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));
        session.setName(request.name());
        session.setDescription(request.description());
        return toResponse(repository.save(session));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Session", id);
        repository.deleteById(id);
        log.info("Session deleted: id={}", id);
    }

    private SessionResponse toResponse(CompilationSession s) {
        return new SessionResponse(
                s.getId(), s.getName(), s.getDescription(),
                s.getCreatedAt(), s.getRecords().size()
        );
    }
}