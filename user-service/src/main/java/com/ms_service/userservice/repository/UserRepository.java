package com.ms_service.userservice.repository;

import com.ms_service.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsUserByUsernameOrEmail(String username, String email);
    Optional<User> findUserByUsername(String username);
}
