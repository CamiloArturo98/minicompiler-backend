package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.AiChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link AiChatHistory} entities.
 * Provides standard CRUD operations plus chronological pagination.
 */
@Repository
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Long> {

    /**
     * Returns a paginated list of all chat history entries sorted by
     * creation date descending (most recent first).
     *
     * @param  pageable pagination and sorting parameters
     * @return a page of {@link AiChatHistory} entries
     */
    Page<AiChatHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}