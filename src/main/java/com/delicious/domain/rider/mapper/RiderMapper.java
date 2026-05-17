package com.delicious.domain.rider.mapper;

import com.delicious.domain.rider.dto.RiderResponse;
import com.delicious.domain.rider.entity.RiderProfile;
import com.delicious.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class RiderMapper {

    public RiderResponse toRiderResponse(User user, RiderProfile profile) {
        if (user == null || profile == null) {
            return null;
        }

        return RiderResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .vehicleType(profile.getVehicleType())
                .vehicleRegistrationNumber(profile.getVehicleRegistrationNumber())
                .currentLocation(profile.getCurrentLocation())
                .status(user.getStatus())
                .build();
    }
}
