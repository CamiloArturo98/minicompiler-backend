package com.minicompiler.service;

import com.minicompiler.domain.entity.Tab;
import com.minicompiler.domain.repository.TabRepository;
import com.minicompiler.domain.repository.UserRepository;
import com.minicompiler.dto.request.TabRequest;
import com.minicompiler.dto.response.TabResponse;
import com.minicompiler.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing persistence and retrieval of editor tabs per user.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TabService {

    private final TabRepository  tabRepository;
    private final UserRepository userRepository;

    // =========================================================================
    // Public API
    // =========================================================================

    /** Returns all tabs for the given user ordered by position. */
    public List<TabResponse> findAllByUser(String username) {
        return tabRepository.findByUserUsernameOrderByPositionAsc(username)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Creates a new tab for the given user.
     *
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public TabResponse create(String username, TabRequest request) {
        var user = userRepository.findByUsername(username).orElseThrow();
        var tab  = Tab.builder()
                .user(user)
                .name(request.name())
                .code(request.code() != null ? request.code() : "")
                .position(request.position())
                .build();
        return toResponse(tabRepository.save(tab));
    }

    /**
     * Updates an existing tab's name, code, and position.
     *
     * @throws ResourceNotFoundException if tab not found or doesn't belong to user
     */
    @Transactional
    public TabResponse update(Long id, String username, TabRequest request) {
        var tab = findOwned(id, username);
        tab.setName(request.name());
        tab.setCode(request.code() != null ? request.code() : "");
        tab.setPosition(request.position());
        return toResponse(tabRepository.save(tab));
    }

    /**
     * Deletes a tab by ID.
     *
     * @throws ResourceNotFoundException if tab not found or doesn't belong to user
     */
    @Transactional
    public void delete(Long id, String username) {
        tabRepository.delete(findOwned(id, username));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Tab findOwned(Long id, String username) {
        return tabRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Tab", id));
    }

    private TabResponse toResponse(Tab t) {
        return new TabResponse(
                t.getId(), t.getName(), t.getCode(),
                t.getPosition(), t.getCreatedAt(), t.getUpdatedAt());
    }
}