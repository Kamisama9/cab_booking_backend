package com.cts.user_service.repository;

import com.cts.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    List<User> findByRole(User.Role role);
    List<User> findByStatus(User.Status status);
    
    // ✅ ADD: Paginated methods
    Page<User> findByRole(User.Role role, Pageable pageable);
    Page<User> findByStatus(User.Status status, Pageable pageable);
    
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    
    long countByRole(User.Role role);
    long countByStatus(User.Status status);
}