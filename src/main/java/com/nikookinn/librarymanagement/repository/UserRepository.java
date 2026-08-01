package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.User;
import com.nikookinn.librarymanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);
}
