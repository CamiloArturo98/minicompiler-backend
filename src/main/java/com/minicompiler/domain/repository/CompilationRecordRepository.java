package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.CompilationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompilationRecordRepository extends JpaRepository<CompilationRecord, Long> {

    List<CompilationRecord> findBySessionIdOrderByCreatedAtDesc(Long sessionId);

    Page<CompilationRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<CompilationRecord> findBySuccessOrderByCreatedAtDesc(boolean success);

    @Query("SELECT COUNT(r) FROM CompilationRecord r WHERE r.success = true")
    long countSuccessful();

    @Query("SELECT COUNT(r) FROM CompilationRecord r WHERE r.success = false")
    long countFailed();

    @Query("SELECT AVG(r.compilationTimeMs) FROM CompilationRecord r")
    Double avgCompilationTime();
}