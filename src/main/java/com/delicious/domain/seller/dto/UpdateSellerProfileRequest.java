package com.delicious.domain.seller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSellerProfileRequest {
    @NotBlank(message = "Store name is required")
    private String storeName;
    private String description;
    private String address;
}
