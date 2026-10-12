package com.delicious.domain.user.dto;

import com.delicious.domain.user.enums.UserRole;
import com.delicious.domain.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    /** Signed JWT access token — include in subsequent requests as: Authorization: Bearer <token> */
    private String token;

    /** Signed JWT refresh token — used to issue new access tokens */
    private String refreshToken;
}
