package com.ms_service.userservice.controller;

import com.ms_service.userservice.dto.LoginResponse;
import com.ms_service.userservice.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

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
    public LoginResponse loginUser (@RequestHeader("Authorization") String authHeader) {
        log.info("inside login controller");
        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));

        String[] values = credentials.split(":", 2);
        String username = values[0];
        String password = values[1];

        return authService.loginUser(username, password);
    }
}
