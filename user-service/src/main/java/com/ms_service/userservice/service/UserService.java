package com.ms_service.userservice.service;

import com.ms_service.userservice.dto.UserRequest;
import com.ms_service.userservice.dto.UserResponse;
import com.ms_service.userservice.model.User;
import com.ms_service.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser (UserRequest request) {
        boolean duplicateUser = userRepository.existsUserByUsernameOrEmail(request.getUsername(), request.getEmail());
        if (duplicateUser){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"user already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        return UserResponse.mapper(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers () {
        return userRepository.findAll().stream().map(UserResponse::mapper).toList();
    }
}
