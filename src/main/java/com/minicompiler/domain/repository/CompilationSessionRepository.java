package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.CompilationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompilationSessionRepository extends JpaRepository<CompilationSession, Long> {
    List<CompilationSession> findAllByOrderByCreatedAtDesc();
}