package com.delicious.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerOrderGroupDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long sellerId;
    private String sellerStoreName;
    private String sellerEmail;
    private String sellerPhone;
    private List<OrderItemDetailDto> items;
}
