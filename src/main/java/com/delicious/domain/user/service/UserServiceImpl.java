package com.delicious.domain.user.service;

import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.enums.UserRole;
import com.delicious.domain.user.enums.UserStatus;
import com.delicious.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public User updateProfile(Long id, String name, String phone) {
        User user = getUserById(id);
        
        if (name != null) {
            user.setName(name);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User createUser(String name, String email, String phone, String password, UserRole role, UserStatus status) {
        User user = User.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .password(password)
                .role(role)
                .status(status)
                .build();
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsersByIds(Collection<Long> ids) {
        return userRepository.findAllById(ids);
    }

    @Override
    @Transactional
    public User updateUserStatus(Long id, UserStatus status) {
        User user = getUserById(id);
        user.setStatus(status);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<User> getUsersByRole(UserRole role, org.springframework.data.domain.Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }
}
