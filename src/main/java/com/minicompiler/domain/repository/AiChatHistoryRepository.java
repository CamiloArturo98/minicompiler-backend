package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.AiChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Long> {
    Page<AiChatHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}