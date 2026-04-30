package com.minicompiler.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_chat_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String userContent;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String assistantContent;

    @Column(length = 50)
    private String action;

    @Column(nullable = false)
    private long responseTimeMs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}