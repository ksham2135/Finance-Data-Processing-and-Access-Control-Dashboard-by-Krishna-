package com.financeapp.repository;

import com.financeapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for User entities.
 * Spring Data JPA provides all CRUD operations automatically.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address (used for login and JWT loading).
     *
     * @param email the email to search for
     * @return Optional<User> — empty if user not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether an email is already registered.
     * More efficient than fetching the full entity just for a boolean check.
     *
     * @param email the email to check
     * @return true if the email already exists
     */
    boolean existsByEmail(String email);
}
