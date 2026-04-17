package com.ms_service.userservice.repository;

import com.ms_service.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsUserByUsernameOrEmail(String username, String email);
}
