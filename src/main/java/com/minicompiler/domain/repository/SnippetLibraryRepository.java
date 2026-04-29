package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.SnippetLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SnippetLibraryRepository extends JpaRepository<SnippetLibrary, Long> {
    List<SnippetLibrary> findByCategoryOrderByLikesDesc(String category);
    List<SnippetLibrary> findAllByOrderByLikesDesc();
    List<SnippetLibrary> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);
}