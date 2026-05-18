package com.delicious.domain.user.service;

import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.enums.UserRole;
import com.delicious.domain.user.enums.UserStatus;
import java.util.Collection;
import java.util.List;

public interface UserService {
    User getUserById(Long id);
    User updateProfile(Long id, String name, String phone);
    User createUser(String name, String email, String phone, String password, UserRole role, UserStatus status);
    List<User> getUsersByIds(Collection<Long> ids);
    User updateUserStatus(Long id, UserStatus status);
    org.springframework.data.domain.Page<User> getUsersByRole(UserRole role, org.springframework.data.domain.Pageable pageable);
}
