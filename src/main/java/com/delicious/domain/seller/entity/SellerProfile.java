package com.delicious.domain.seller.entity;

import com.delicious.common.entity.BaseEntity;
import com.delicious.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seller_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String storeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String address;
}
