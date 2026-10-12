package com.delicious.domain.user.service;

import com.delicious.common.security.CustomUserDetailsService;
import com.delicious.common.security.JwtService;
import com.delicious.domain.user.dto.AuthResponse;
import com.delicious.domain.user.dto.LoginRequest;
import com.delicious.domain.user.dto.RegisterRequest;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.exception.InvalidCredentialsException;
import com.delicious.domain.user.exception.UserAlreadyExistsException;
import com.delicious.domain.user.mapper.UserMapper;
import com.delicious.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = userMapper.toEntity(request, encodedPassword);
        User savedUser = userRepository.save(newUser);

        // Generate JWT access and refresh tokens immediately after registration
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtService.generateToken(userDetails, savedUser.getId());
        String refreshToken = jwtService.generateRefreshToken(userDetails, savedUser.getId());

        AuthResponse response = userMapper.toAuthResponse(savedUser);
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, user.getId());
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

        AuthResponse response = userMapper.toAuthResponse(user);
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidCredentialsException("Refresh token is required");
        }

        try {
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new InvalidCredentialsException("Invalid token type. Expected a refresh token.");
            }

            final String email = jwtService.extractEmail(refreshToken);
            if (email == null) {
                throw new InvalidCredentialsException("Invalid refresh token payload");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidCredentialsException("User associated with this token not found"));

            if (user.getStatus() != com.delicious.domain.user.enums.UserStatus.ACTIVE) {
                throw new InvalidCredentialsException("User account is inactive or banned");
            }

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new InvalidCredentialsException("Refresh token is expired or invalid");
            }

            // Issue a fresh access token and rotated refresh token
            String newAccessToken = jwtService.generateToken(userDetails, user.getId());
            String newRefreshToken = jwtService.generateRefreshToken(userDetails, user.getId());

            AuthResponse response = userMapper.toAuthResponse(user);
            response.setToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            return response;
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid or expired refresh token: " + e.getMessage());
        }
    }
}
