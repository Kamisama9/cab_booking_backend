package com.cts.user_service.dao;

import com.cts.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    List<User> findByRole(User.Role role);
    List<User> findByStatus(User.Status status);
    List<User> findAll();
    
    // ✅ ADD: Paginated methods
    Page<User> findByRole(User.Role role, Pageable pageable);
    Page<User> findByStatus(User.Status status, Pageable pageable);
    Page<User> findAll(Pageable pageable);
    
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    void deleteById(String id);
    
    long count();
    long countByRole(User.Role role);
    long countByStatus(User.Status status);
}