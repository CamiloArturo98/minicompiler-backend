package com.minicompiler.service;

import com.minicompiler.domain.repository.CompilationRecordRepository;
import com.minicompiler.domain.repository.CompilationSessionRepository;
import com.minicompiler.domain.repository.SnippetLibraryRepository;
import com.minicompiler.dto.response.StatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final CompilationRecordRepository recordRepository;
    private final CompilationSessionRepository sessionRepository;
    private final SnippetLibraryRepository snippetRepository;

    public StatsResponse getStats() {
        long total      = recordRepository.count();
        long successful = recordRepository.countSuccessful();
        long failed     = recordRepository.countFailed();
        double rate     = total > 0 ? (successful * 100.0 / total) : 0.0;
        Double avgTime  = recordRepository.avgCompilationTime();

        return new StatsResponse(
                total, successful, failed,
                Math.round(rate * 100.0) / 100.0,
                avgTime != null ? Math.round(avgTime * 100.0) / 100.0 : 0.0,
                sessionRepository.count(),
                snippetRepository.count()
        );
    }
}