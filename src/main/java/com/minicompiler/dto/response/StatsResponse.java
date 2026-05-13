package com.minicompiler.dto.response;

/**
 * Response body carrying aggregated compiler usage statistics.
 *
 * @param totalCompilations    total number of compilation attempts
 * @param successful           number of compilations that completed without errors
 * @param failed               number of compilations that produced an error
 * @param successRate          percentage of successful compilations (0.0 – 100.0)
 * @param avgCompilationTimeMs average compilation time in milliseconds
 * @param totalSessions        total number of compilation sessions created
 * @param totalSnippets        total number of saved snippets in the library
 */
public record StatsResponse(
        long   totalCompilations,
        long   successful,
        long   failed,
        double successRate,
        double avgCompilationTimeMs,
        long   totalSessions,
        long   totalSnippets
) {}