package com.delicious.domain.rider.dto;

import com.delicious.domain.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String vehicleType;
    private String vehicleRegistrationNumber;
    private String currentLocation;
    private UserStatus status;
}
