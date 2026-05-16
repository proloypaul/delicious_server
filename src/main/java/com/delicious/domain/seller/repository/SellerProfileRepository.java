package com.delicious.domain.seller.repository;

import com.delicious.domain.seller.entity.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUserId(Long userId);
    java.util.List<SellerProfile> findAllByUserIdIn(java.util.Collection<Long> userIds);
}
