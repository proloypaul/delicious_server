package com.delicious.domain.seller.service;

import com.delicious.domain.seller.dto.SellerProfileResponse;
import com.delicious.domain.seller.dto.SellerRegistrationRequest;
import com.delicious.domain.seller.dto.UpdateSellerProfileRequest;
import java.util.List;
import java.util.Map;

public interface SellerService {
    SellerProfileResponse register(SellerRegistrationRequest request);
    SellerProfileResponse getProfile(Long userId);
    SellerProfileResponse updateProfile(Long userId, UpdateSellerProfileRequest request);
    Map<Long, SellerProfileResponse> getSellerProfilesByUserIds(List<Long> userIds);
}
