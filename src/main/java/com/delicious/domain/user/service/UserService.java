package com.delicious.domain.user.service;

import com.delicious.domain.user.entity.User;

public interface UserService {
    User getUserById(Long id);
    User updateProfile(Long id, String name, String phone);
}
