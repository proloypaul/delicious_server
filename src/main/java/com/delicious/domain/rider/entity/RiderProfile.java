package com.delicious.domain.rider.entity;

import com.delicious.common.entity.BaseEntity;
import com.delicious.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rider_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false)
    private String vehicleRegistrationNumber;

    @Column
    private String currentLocation;
}
