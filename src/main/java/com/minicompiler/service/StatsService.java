package com.minicompiler.service;

import com.minicompiler.domain.repository.CompilationRecordRepository;
import com.minicompiler.domain.repository.CompilationSessionRepository;
import com.minicompiler.domain.repository.SnippetLibraryRepository;
import com.minicompiler.dto.response.StatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that aggregates compiler usage statistics from the database.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final double ROUNDING_SCALE = 100.0;

    // =========================================================================
    // Dependencies
    // =========================================================================

    private final CompilationRecordRepository  recordRepository;
    private final CompilationSessionRepository sessionRepository;
    private final SnippetLibraryRepository     snippetRepository;

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Collects and returns the current aggregated statistics.
     *
     * @return a {@link StatsResponse} with totals, success rate, and averages
     */
    public StatsResponse getStats() {
        long   total      = recordRepository.count();
        long   successful = recordRepository.countSuccessful();
        long   failed     = recordRepository.countFailed();
        double rate       = total > 0 ? (successful * ROUNDING_SCALE / total) : 0.0;
        Double avgTime    = recordRepository.avgCompilationTime();

        return new StatsResponse(
                total, successful, failed,
                round(rate),
                avgTime != null ? round(avgTime) : 0.0,
                sessionRepository.count(),
                snippetRepository.count()
        );
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private double round(double value) {
        return Math.round(value * ROUNDING_SCALE) / ROUNDING_SCALE;
    }
}