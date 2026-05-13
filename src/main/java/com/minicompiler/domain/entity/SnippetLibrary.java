package com.minicompiler.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a user-saved code snippet stored in the
 * {@code snippet_library} table.
 */
@Entity
@Table(name = "snippet_library")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SnippetLibrary {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short display title for the snippet. */
    @Column(nullable = false, length = 100)
    private String title;

    /** Optional description explaining what the snippet does. */
    @Column(length = 300)
    private String description;

    /** The full source code of the snippet. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    /** Category tag used for filtering (e.g. {@code functions}, {@code loops}). */
    @Column(length = 50)
    private String category;

    /** Number of likes received; defaults to {@code 0} on creation. */
    @Column(nullable = false)
    @Builder.Default
    private int likes = 0;

    /** Timestamp set automatically at insert time, never updated. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp updated automatically on every modification. */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}