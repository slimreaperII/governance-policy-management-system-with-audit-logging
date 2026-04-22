package com.ms_service.userservice.controller;

import com.ms_service.userservice.dto.UserRequest;
import com.ms_service.userservice.dto.UserResponse;
import com.ms_service.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser (@RequestBody @Valid UserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getAllUsers () {
        return userService.getAllUsers();
    }
}
