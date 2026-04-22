package com.ms_service.userservice.service;

import com.ms_service.userservice.dto.LoginResponse;
import com.ms_service.userservice.model.User;
import com.ms_service.userservice.repository.UserRepository;
import com.ms_service.userservice.util.JwtGetToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class AuthService {

    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    public LoginResponse loginUser(String username, String password) {

        log.info("inside login method");

        User user = repository.findUserByUsername(username).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));

        if (!user.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST , "Incorrect password. Try again!");
        }

        String token = JwtGetToken.generateToken(user.getUsername(), user.getRole());

        return new LoginResponse(user.getUsername(), token);
    }
}
