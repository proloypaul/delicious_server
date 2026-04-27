package com.delicious.domain.customer.mapper;

import com.delicious.domain.customer.dto.CustomerProfileResponse;
import com.delicious.domain.customer.entity.CustomerProfile;
import com.delicious.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerProfileResponse toProfileResponse(User user, CustomerProfile customerProfile) {
        if (user == null) {
            return null;
        }

        String address = (customerProfile != null) ? customerProfile.getAddress() : null;

        return CustomerProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(address)
                .status(user.getStatus())
                .build();
    }
}
