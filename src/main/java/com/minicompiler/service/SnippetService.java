package com.minicompiler.service;

import com.minicompiler.domain.entity.SnippetLibrary;
import com.minicompiler.domain.repository.SnippetLibraryRepository;
import com.minicompiler.dto.request.SnippetRequest;
import com.minicompiler.dto.response.SnippetResponse;
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
public class SnippetService {

    private final SnippetLibraryRepository repository;

    public List<SnippetResponse> findAll() {
        return repository.findAllByOrderByLikesDesc().stream().map(this::toResponse).toList();
    }

    public SnippetResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", id));
    }

    public List<SnippetResponse> findByCategory(String category) {
        return repository.findByCategoryOrderByLikesDesc(category).stream().map(this::toResponse).toList();
    }

    public List<SnippetResponse> search(String query) {
        return repository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(query)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public SnippetResponse create(SnippetRequest request) {
        SnippetLibrary snippet = SnippetLibrary.builder()
                .title(request.title())
                .description(request.description())
                .code(request.code())
                .category(request.category())
                .build();
        return toResponse(repository.save(snippet));
    }

    @Transactional
    public SnippetResponse update(Long id, SnippetRequest request) {
        SnippetLibrary snippet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", id));
        snippet.setTitle(request.title());
        snippet.setDescription(request.description());
        snippet.setCode(request.code());
        snippet.setCategory(request.category());
        return toResponse(repository.save(snippet));
    }

    @Transactional
    public SnippetResponse like(Long id) {
        SnippetLibrary snippet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", id));
        snippet.setLikes(snippet.getLikes() + 1);
        return toResponse(repository.save(snippet));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Snippet", id);
        repository.deleteById(id);
    }

    private SnippetResponse toResponse(SnippetLibrary s) {
        return new SnippetResponse(s.getId(), s.getTitle(), s.getDescription(),
                s.getCode(), s.getCategory(), s.getLikes(), s.getCreatedAt(), s.getUpdatedAt());
    }
}