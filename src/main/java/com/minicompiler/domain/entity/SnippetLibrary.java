package com.minicompiler.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "snippet_library")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SnippetLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 300)
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private int likes = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}