package com.ms_service.userservice.controller;

import com.ms_service.userservice.dto.LoginRequest;
import com.ms_service.userservice.dto.LoginResponse;
import com.ms_service.userservice.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public LoginResponse loginUser (@RequestBody LoginRequest request) {
        return authService.loginUser(request);
    }
}
