package com.delicious.domain.notification.service;

import com.delicious.domain.notification.dto.OrderCreatedNotificationEvent;
import com.delicious.domain.notification.dto.OrderDispatchedNotificationEvent;
import com.delicious.domain.notification.dto.SellerOrderGroupDto;

public interface EmailService {
    void sendSellerNewOrderAlert(SellerOrderGroupDto sellerGroup, OrderCreatedNotificationEvent orderEvent);
    void sendCustomerOrderOnTheWay(OrderDispatchedNotificationEvent event);
}
