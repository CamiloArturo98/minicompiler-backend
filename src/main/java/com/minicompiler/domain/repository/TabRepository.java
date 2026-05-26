package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.Tab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link Tab} entities.
 */
@Repository
public interface TabRepository extends JpaRepository<Tab, Long> {

    List<Tab> findByUserUsernameOrderByPositionAsc(String username);

    Optional<Tab> findByIdAndUserUsername(Long id, String username);
}