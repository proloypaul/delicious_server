package com.delicious.domain.customer.dto;

import com.delicious.domain.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProfileResponse {
    private String name;
    private String email;
    private String phone;
    private String address;
    private UserStatus status;
}
