package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.SnippetLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link SnippetLibrary} entities.
 * Provides standard CRUD operations plus filtered and sorted queries.
 */
@Repository
public interface SnippetLibraryRepository extends JpaRepository<SnippetLibrary, Long> {

    /**
     * Returns all snippets in the given category, sorted by likes descending.
     *
     * @param category the category tag to filter by
     */
    List<SnippetLibrary> findByCategoryOrderByLikesDesc(String category);

    /** Returns all snippets sorted by likes descending. */
    List<SnippetLibrary> findAllByOrderByLikesDesc();

    /**
     * Returns all snippets whose title contains the given string
     * (case-insensitive), sorted by creation date descending.
     *
     * @param title the partial title string to search for
     */
    List<SnippetLibrary> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);
}