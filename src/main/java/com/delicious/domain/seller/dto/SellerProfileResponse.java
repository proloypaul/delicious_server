package com.delicious.domain.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerProfileResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String storeName;
    private String description;
    private String address;
    private String status;
}
