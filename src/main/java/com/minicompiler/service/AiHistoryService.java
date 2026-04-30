package com.minicompiler.service;

import com.minicompiler.domain.entity.AiChatHistory;
import com.minicompiler.domain.repository.AiChatHistoryRepository;
import com.minicompiler.dto.response.AiHistoryResponse;
import com.minicompiler.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiHistoryService {

    private final AiChatHistoryRepository repository;

    public List<AiHistoryResponse> findRecent(int limit) {
        Page<AiChatHistory> page = repository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(0, limit));
        return page.getContent().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AiHistoryResponse save(String userContent, String assistantContent,
                                  String action, long responseTimeMs) {
        AiChatHistory saved = repository.save(AiChatHistory.builder()
                .userContent(userContent)
                .assistantContent(assistantContent)
                .action(action)
                .responseTimeMs(responseTimeMs)
                .build());
        return toResponse(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("AiHistory", id);
        repository.deleteById(id);
    }

    @Transactional
    public void clearAll() {
        repository.deleteAll();
    }

    private AiHistoryResponse toResponse(AiChatHistory h) {
        return new AiHistoryResponse(
                h.getId(), h.getUserContent(), h.getAssistantContent(),
                h.getAction(), h.getResponseTimeMs(), h.getCreatedAt());
    }
}