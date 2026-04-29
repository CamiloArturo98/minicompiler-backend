package com.minicompiler.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "compilation_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompilationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private CompilationSession session;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String sourceCode;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT")
    private String bytecode;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false)
    private long compilationTimeMs;

    @Column(nullable = false)
    private int instructionsExecuted;

    @Column(nullable = false)
    private boolean optimized;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}