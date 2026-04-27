package com.delicious.domain.user.service;

import com.delicious.domain.user.dto.AuthResponse;
import com.delicious.domain.user.dto.LoginRequest;
import com.delicious.domain.user.dto.RegisterRequest;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest request);
    AuthResponse loginUser(LoginRequest request);
}
