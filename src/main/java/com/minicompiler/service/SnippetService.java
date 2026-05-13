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

/**
 * Service managing persistence and retrieval of {@link SnippetLibrary} entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnippetService {

    private final SnippetLibraryRepository repository;

    // =========================================================================
    // Public API
    // =========================================================================

    /** Returns all snippets ordered by likes descending. */
    public List<SnippetResponse> findAll() {
        return repository.findAllByOrderByLikesDesc()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Returns a single snippet by its ID.
     *
     * @param  id the snippet ID
     * @throws ResourceNotFoundException if no snippet exists with the given ID
     */
    public SnippetResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", id));
    }

    /**
     * Returns all snippets in the given category, ordered by likes descending.
     *
     * @param category the category tag to filter by
     */
    public List<SnippetResponse> findByCategory(String category) {
        return repository.findByCategoryOrderByLikesDesc(category)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Returns all snippets whose title contains {@code query} (case-insensitive),
     * ordered by creation date descending.
     *
     * @param query the partial title string to search for
     */
    public List<SnippetResponse> search(String query) {
        return repository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(query)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Creates and persists a new snippet.
     *
     * @param  request the validated snippet request body
     * @return the created snippet response
     */
    @Transactional
    public SnippetResponse create(SnippetRequest request) {
        var snippet = SnippetLibrary.builder()
                .title(request.title())
                .description(request.description())
                .code(request.code())
                .category(request.category())
                .build();
        return toResponse(repository.save(snippet));
    }

    /**
     * Updates an existing snippet's fields.
     *
     * @param  id      the snippet ID to update
     * @param  request the validated update request body
     * @throws ResourceNotFoundException if no snippet exists with the given ID
     */
    @Transactional
    public SnippetResponse update(Long id, SnippetRequest request) {
        var snippet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", id));
        snippet.setTitle(request.title());
        snippet.setDescription(request.description());
        snippet.setCode(request.code());
        snippet.setCategory(request.category());
        return toResponse(repository.save(snippet));
    }

    /**
     * Increments the like count of a snippet by one.
     *
     * @param  id the snippet ID to like
     * @throws ResourceNotFoundException if no snippet exists with the given ID
     */
    @Transactional
    public SnippetResponse like(Long id) {
        var snippet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", id));
        snippet.setLikes(snippet.getLikes() + 1);
        return toResponse(repository.save(snippet));
    }

    /**
     * Deletes a snippet by its ID.
     *
     * @param  id the snippet ID to delete
     * @throws ResourceNotFoundException if no snippet exists with the given ID
     */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Snippet", id);
        repository.deleteById(id);
    }

    // =========================================================================
    // Mapper
    // =========================================================================

    private SnippetResponse toResponse(SnippetLibrary s) {
        return new SnippetResponse(
                s.getId(), s.getTitle(), s.getDescription(),
                s.getCode(), s.getCategory(), s.getLikes(),
                s.getCreatedAt(), s.getUpdatedAt()
        );
    }
}