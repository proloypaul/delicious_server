package com.delicious.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDispatchedNotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private Long riderId;
    private String riderName;
    private String riderPhone;
    private String vehicleRegistrationNumber;
    private String newStatus;
    private LocalDateTime dispatchedAt;
}
