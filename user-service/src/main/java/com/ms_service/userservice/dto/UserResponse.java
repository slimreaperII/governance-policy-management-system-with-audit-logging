package com.ms_service.userservice.dto;

import com.ms_service.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Integer id;
    private String name;
    private String username;
    private String email;
    private String role;

    public static UserResponse mapper (User user) {
        return new UserResponse(
          user.getId(),
          user.getName(),
          user.getUsername(),
          user.getEmail(),
          user.getRole()
        );
    }
}