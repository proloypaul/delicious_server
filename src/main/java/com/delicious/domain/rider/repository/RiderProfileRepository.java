package com.delicious.domain.rider.repository;

import com.delicious.domain.rider.entity.RiderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderProfileRepository extends JpaRepository<RiderProfile, Long> {
    Optional<RiderProfile> findByUserId(Long userId);
    java.util.List<RiderProfile> findAllByUserIdIn(java.util.Collection<Long> userIds);
}
