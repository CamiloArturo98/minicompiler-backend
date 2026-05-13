package com.minicompiler.dto.response;

import java.util.List;

/**
 * Generic paginated response wrapper used across list endpoints.
 *
 * @param <T>           the type of elements in this page
 * @param content       the list of elements in the current page
 * @param page          current page index (0-based)
 * @param size          number of elements per page
 * @param totalElements total number of elements across all pages
 * @param totalPages    total number of available pages
 * @param last          {@code true} if this is the last page
 */
public record PageResponse<T>(
        List<T> content,
        int     page,
        int     size,
        long    totalElements,
        int     totalPages,
        boolean last
) {}