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

        // Generate JWT immediately after registration so the user is logged in
        String token = generateTokenForUser(savedUser);
        AuthResponse response = userMapper.toAuthResponse(savedUser);
        response.setToken(token);
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

        String token = generateTokenForUser(user);
        AuthResponse response = userMapper.toAuthResponse(user);
        response.setToken(token);
        return response;
    }

    /**
     * Loads the UserDetails for the user and generates a signed JWT.
     * Kept private to avoid code duplication between register and login.
     */
    private String generateTokenForUser(User user) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        return jwtService.generateToken(userDetails, user.getId());
    }
}
