package com.minicompiler.domain.repository;

import com.minicompiler.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param  username the login name to search for
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks whether a username is already taken.
     *
     * @param username the login name to check
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether an email address is already registered.
     *
     * @param email the email to check
     */
    boolean existsByEmail(String email);
}