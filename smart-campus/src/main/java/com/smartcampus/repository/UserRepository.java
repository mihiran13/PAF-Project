package com.smartcampus.repository;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthId(String oauthId);

    Optional<User> findByEmail(String email);

    boolean existsByOauthId(String oauthId);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    Page<User> findAll(Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);
}
