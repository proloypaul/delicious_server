package com.delicious.domain.admin.dto;

/**
 * JPA projection for the single-query user-role count.
 * Maps to the scalar expressions in UserRepository.countAllRoles().
 */
public interface AdminUserCountProjection {
    long getCustomers();
    long getSellers();
    long getRiders();
}
