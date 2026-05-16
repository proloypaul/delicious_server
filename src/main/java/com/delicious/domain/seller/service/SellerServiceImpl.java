package com.delicious.domain.seller.service;

import com.delicious.domain.product.dto.ProductResponse;
import com.delicious.domain.product.service.ProductService;
import com.delicious.domain.seller.dto.SellerProfileResponse;
import com.delicious.domain.seller.dto.SellerRegistrationRequest;
import com.delicious.domain.seller.dto.UpdateSellerProfileRequest;
import com.delicious.domain.seller.entity.SellerProfile;
import com.delicious.domain.seller.mapper.SellerMapper;
import com.delicious.domain.seller.repository.SellerProfileRepository;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.enums.UserRole;
import com.delicious.domain.user.enums.UserStatus;
import com.delicious.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final UserService userService;
    private final SellerProfileRepository sellerProfileRepository;
    private final SellerMapper sellerMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SellerProfileResponse register(SellerRegistrationRequest request) {
        // Create User with ROLE_SELLER and INACTIVE status
        User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                UserRole.SELLER,
                UserStatus.INACTIVE
        );

        // Create initial SellerProfile
        SellerProfile profile = SellerProfile.builder()
                .user(user)
                .storeName(request.getStoreName())
                .build();
        sellerProfileRepository.save(profile);

        return sellerMapper.toProfileResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerProfileResponse getProfile(Long userId) {
        User user = userService.getUserById(userId);
        SellerProfile profile = sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller profile not found for user: " + userId));
        return sellerMapper.toProfileResponse(user, profile);
    }

    @Override
    @Transactional
    public SellerProfileResponse updateProfile(Long userId, UpdateSellerProfileRequest request) {
        User user = userService.getUserById(userId);
        SellerProfile profile = sellerProfileRepository.findByUserId(userId)
                .orElseGet(() -> SellerProfile.builder().user(user).build());

        profile.setStoreName(request.getStoreName());
        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }

        profile = sellerProfileRepository.save(profile);
        return sellerMapper.toProfileResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<Long, SellerProfileResponse> getSellerProfilesByUserIds(java.util.List<Long> userIds) {
        java.util.List<User> users = userService.getUsersByIds(userIds);
        java.util.List<SellerProfile> profiles = sellerProfileRepository.findAllByUserIdIn(userIds);

        java.util.Map<Long, SellerProfile> profileMap = profiles.stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.getUser().getId(), p -> p));

        return users.stream()
                .collect(java.util.stream.Collectors.toMap(
                        User::getId,
                        u -> sellerMapper.toProfileResponse(u, profileMap.get(u.getId()))
                ));
    }
}
