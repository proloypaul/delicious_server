package com.delicious.domain.user.service;

import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id)); // Standard exception for now, can be custom
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
}
