package com.delicious.domain.customer.service;

import com.delicious.domain.customer.dto.CustomerProfileResponse;
import com.delicious.domain.customer.dto.UpdateCustomerProfileRequest;

public interface CustomerService {
    CustomerProfileResponse getProfile(Long userId);
    CustomerProfileResponse updateProfile(Long userId, UpdateCustomerProfileRequest request);
    org.springframework.data.domain.Page<CustomerProfileResponse> getAllCustomers(org.springframework.data.domain.Pageable pageable);
}
