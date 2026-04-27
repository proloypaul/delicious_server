package com.delicious.domain.user.mapper;

import com.delicious.domain.user.dto.AuthResponse;
import com.delicious.domain.user.dto.RegisterRequest;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.enums.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request, String encodedPassword) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(encodedPassword)
                .role(request.getRole())
                .status(UserStatus.ACTIVE) // Default status on registration
                .build();
    }

    public AuthResponse toAuthResponse(User user) {
        if (user == null) {
            return null;
        }

        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
