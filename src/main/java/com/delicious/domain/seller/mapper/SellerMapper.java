package com.delicious.domain.seller.mapper;

import com.delicious.domain.seller.dto.SellerProfileResponse;
import com.delicious.domain.seller.entity.SellerProfile;
import com.delicious.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class SellerMapper {

    public SellerProfileResponse toProfileResponse(User user, SellerProfile profile) {
        if (user == null) return null;

        SellerProfileResponse.SellerProfileResponseBuilder builder = SellerProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus().name());

        if (profile != null) {
            builder.storeName(profile.getStoreName())
                    .description(profile.getDescription())
                    .address(profile.getAddress());
        }

        return builder.build();
    }
}
