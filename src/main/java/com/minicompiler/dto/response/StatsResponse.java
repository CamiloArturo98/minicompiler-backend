package com.minicompiler.dto.response;

public record StatsResponse(
        long totalCompilations,
        long successful,
        long failed,
        double successRate,
        double avgCompilationTimeMs,
        long totalSessions,
        long totalSnippets
) {}