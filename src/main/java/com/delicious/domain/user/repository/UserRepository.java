package com.delicious.domain.user.repository;

import com.delicious.domain.admin.dto.AdminUserCountProjection;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Page<User> findByRole(UserRole role, Pageable pageable);
    long countByRole(UserRole role);

    /**
     * Returns customer / seller / rider counts in a SINGLE database round-trip
     * using conditional aggregation — replaces three separate countByRole() calls.
     */
    @Query("""
            SELECT
                SUM(CASE WHEN u.role = com.delicious.domain.user.enums.UserRole.CUSTOMER THEN 1 ELSE 0 END) AS customers,
                SUM(CASE WHEN u.role = com.delicious.domain.user.enums.UserRole.SELLER   THEN 1 ELSE 0 END) AS sellers,
                SUM(CASE WHEN u.role = com.delicious.domain.user.enums.UserRole.RIDER    THEN 1 ELSE 0 END) AS riders
            FROM User u
            """)
    AdminUserCountProjection countAllRoles();
}
