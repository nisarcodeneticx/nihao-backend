package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR :search = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR :status = '' OR u.status = :status)")
    Page<User> searchUsers(@Param("search") String search, @Param("status") String status, Pageable pageable);
}
