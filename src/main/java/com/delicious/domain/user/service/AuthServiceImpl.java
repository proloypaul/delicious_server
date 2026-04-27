package com.delicious.domain.user.service;

import com.delicious.domain.user.dto.AuthResponse;
import com.delicious.domain.user.dto.LoginRequest;
import com.delicious.domain.user.dto.RegisterRequest;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.exception.InvalidCredentialsException;
import com.delicious.domain.user.exception.UserAlreadyExistsException;
import com.delicious.domain.user.mapper.UserMapper;
import com.delicious.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = userMapper.toEntity(request, encodedPassword);
        
        User savedUser = userRepository.save(newUser);
        return userMapper.toAuthResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Logic for generating JWT goes here

        return userMapper.toAuthResponse(user);
    }
}
