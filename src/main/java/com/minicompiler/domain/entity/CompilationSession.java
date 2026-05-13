package com.minicompiler.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a named compilation session stored in the
 * {@code compilation_sessions} table.
 * A session groups multiple {@link CompilationRecord} entries under a shared context.
 */
@Entity
@Table(name = "compilation_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompilationSession {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name for this session. */
    @Column(nullable = false, length = 100)
    private String name;

    /** Optional description providing context for the session. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Timestamp set automatically at insert time, never updated. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Compilation records belonging to this session.
     * Cascades all operations and removes orphaned records automatically.
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CompilationRecord> records = new ArrayList<>();
}