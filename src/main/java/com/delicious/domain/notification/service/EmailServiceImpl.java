package com.delicious.domain.notification.service;

import com.delicious.domain.notification.dto.OrderItemDetailDto;
import com.delicious.domain.notification.dto.OrderCreatedNotificationEvent;
import com.delicious.domain.notification.dto.OrderDispatchedNotificationEvent;
import com.delicious.domain.notification.dto.SellerOrderGroupDto;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from:noreply@delicious.com}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sendSellerNewOrderAlert(SellerOrderGroupDto sellerGroup, OrderCreatedNotificationEvent orderEvent) {
        String recipient = sellerGroup.getSellerEmail();
        if (recipient == null || recipient.trim().isEmpty()) {
            log.warn("Cannot send email to seller ID: {} (store: {}) because email address is missing.",
                    sellerGroup.getSellerId(), sellerGroup.getSellerStoreName());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Delicious App");
            helper.setTo(recipient);
            helper.setSubject("🔔 New Customer Order Alert! Order #" + orderEvent.getOrderId());

            String htmlBody = buildSellerOrderHtml(sellerGroup, orderEvent);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Successfully sent new order alert email to seller: {} for order #{}", recipient, orderEvent.getOrderId());
        } catch (Exception ex) {
            log.error("Failed to send order alert email to seller: {} for order #{}", recipient, orderEvent.getOrderId(), ex);
        }
    }

    @Override
    public void sendCustomerOrderOnTheWay(OrderDispatchedNotificationEvent event) {
        String recipient = event.getCustomerEmail();
        if (recipient == null || recipient.trim().isEmpty()) {
            log.warn("Cannot send dispatch notification to customer ID: {} for order #{} because email is missing.",
                    event.getCustomerId(), event.getOrderId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Delicious App");
            helper.setTo(recipient);
            helper.setSubject("🚀 Your Order #" + event.getOrderId() + " is On The Way!");

            String htmlBody = buildCustomerDispatchedHtml(event);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Successfully sent dispatch notification email to customer: {} for order #{}", recipient, event.getOrderId());
        } catch (Exception ex) {
            log.error("Failed to send dispatch email to customer: {} for order #{}", recipient, event.getOrderId(), ex);
        }
    }

    private String buildSellerOrderHtml(SellerOrderGroupDto sellerGroup, OrderCreatedNotificationEvent orderEvent) {
        StringBuilder itemsTable = new StringBuilder();
        for (OrderItemDetailDto item : sellerGroup.getItems()) {
            itemsTable.append("<tr style=\"border-bottom: 1px solid #f1f5f9;\">")
                    .append("<td style=\"padding: 10px 12px; font-weight: 500;\">").append(escapeHtml(item.getFoodName())).append("</td>")
                    .append("<td style=\"padding: 10px 12px; text-align: center;\">").append(item.getQuantity()).append("</td>")
                    .append("<td style=\"padding: 10px 12px; text-align: right;\">$").append(item.getUnitPrice()).append("</td>")
                    .append("<td style=\"padding: 10px 12px; text-align: right; font-weight: 600;\">$").append(item.getTotalPrice()).append("</td>")
                    .append("</tr>");
        }

        String formattedTime = orderEvent.getCreatedAt() != null ? orderEvent.getCreatedAt().format(DATE_FORMATTER) : "Just now";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.07); overflow: hidden; border: 1px solid #e2e8f0; }
                        .header { background: linear-gradient(135deg, #f97316 0%%, #ea580c 100%%); color: #ffffff; padding: 28px 24px; text-align: center; }
                        .content { padding: 24px; }
                        .badge { display: inline-block; background-color: #fef3c7; color: #b45309; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; text-transform: uppercase; margin-bottom: 12px; }
                        .info-box { background: #f8fafc; border-left: 4px solid #ea580c; border-radius: 6px; padding: 14px 16px; margin: 18px 0; }
                        table { width: 100%%; border-collapse: collapse; margin-top: 14px; font-size: 14px; }
                        th { background: #f1f5f9; padding: 10px 12px; text-align: left; color: #64748b; font-weight: 600; font-size: 12px; text-transform: uppercase; }
                        .footer { background: #f1f5f9; text-align: center; padding: 16px; font-size: 12px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1 style="margin: 0; font-size: 22px; font-weight: 700;">Delicious Store Notification</h1>
                            <p style="margin: 6px 0 0; opacity: 0.9; font-size: 14px;">You have received a new customer order!</p>
                        </div>
                        <div class="content">
                            <span class="badge">Store: %s</span>
                            <h2 style="font-size: 18px; margin: 0 0 10px;">Order #%d</h2>
                            <p style="margin: 0 0 16px; color: #64748b; font-size: 13px;">Placed at: %s</p>

                            <div class="info-box">
                                <strong style="display: block; margin-bottom: 4px; color: #0f172a;">Customer Details:</strong>
                                <div><strong>Name:</strong> %s</div>
                                <div><strong>Phone:</strong> %s</div>
                                <div><strong>Delivery Address:</strong> %s</div>
                            </div>

                            <h3 style="font-size: 15px; margin: 20px 0 8px; color: #334155;">Ordered Items For Your Store:</h3>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Item</th>
                                        <th style="text-align: center;">Qty</th>
                                        <th style="text-align: right;">Unit</th>
                                        <th style="text-align: right;">Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    %s
                                </tbody>
                            </table>
                        </div>
                        <div class="footer">
                            &copy; Delicious Platform. All rights reserved. Automatic event-driven notification.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(sellerGroup.getSellerStoreName() != null ? sellerGroup.getSellerStoreName() : "Seller"),
                orderEvent.getOrderId(),
                formattedTime,
                escapeHtml(orderEvent.getCustomerName() != null ? orderEvent.getCustomerName() : "Customer"),
                escapeHtml(orderEvent.getCustomerPhone() != null ? orderEvent.getCustomerPhone() : "N/A"),
                escapeHtml(orderEvent.getDeliveryAddress() != null ? orderEvent.getDeliveryAddress() : "N/A"),
                itemsTable.toString()
        );
    }

    private String buildCustomerDispatchedHtml(OrderDispatchedNotificationEvent event) {
        String formattedTime = event.getDispatchedAt() != null ? event.getDispatchedAt().format(DATE_FORMATTER) : "Just now";
        String riderName = event.getRiderName() != null ? event.getRiderName() : "Our Delivery Partner";
        String riderPhone = event.getRiderPhone() != null ? event.getRiderPhone() : "N/A";
        String vehicle = event.getVehicleRegistrationNumber() != null ? event.getVehicleRegistrationNumber() : "Dispatched";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.07); overflow: hidden; border: 1px solid #e2e8f0; }
                        .header { background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); color: #ffffff; padding: 28px 24px; text-align: center; }
                        .content { padding: 24px; }
                        .rider-card { background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 8px; padding: 18px; margin: 20px 0; }
                        .rider-card h3 { margin: 0 0 10px; color: #065f46; font-size: 16px; }
                        .detail-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
                        .footer { background: #f1f5f9; text-align: center; padding: 16px; font-size: 12px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1 style="margin: 0; font-size: 22px; font-weight: 700;">Delicious Order Update</h1>
                            <p style="margin: 6px 0 0; opacity: 0.9; font-size: 15px;">Great news! Your order is on the way!</p>
                        </div>
                        <div class="content">
                            <h2 style="font-size: 18px; margin: 0 0 6px;">Hello %s,</h2>
                            <p style="margin: 0 0 14px; color: #475569; font-size: 14px; line-height: 1.5;">
                                Your order <strong>#%d</strong> has been picked up by our delivery partner and is speeding toward you!
                            </p>

                            <div class="rider-card">
                                <h3>🚴 Delivery Partner Details</h3>
                                <div class="detail-row"><span>Rider Name:</span><strong>%s</strong></div>
                                <div class="detail-row"><span>Contact Phone:</span><strong>%s</strong></div>
                                <div class="detail-row"><span>Vehicle Info:</span><strong>%s</strong></div>
                                <div class="detail-row"><span>Dispatched At:</span><span>%s</span></div>
                            </div>

                            <div style="background: #f8fafc; border-radius: 8px; padding: 14px 16px; border: 1px solid #e2e8f0; font-size: 14px;">
                                <strong>Delivery Address:</strong><br>
                                <span style="color: #64748b;">%s</span>
                            </div>
                        </div>
                        <div class="footer">
                            &copy; Delicious Platform. Enjoy your meal!
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(event.getCustomerName() != null ? event.getCustomerName() : "Valued Customer"),
                event.getOrderId(),
                escapeHtml(riderName),
                escapeHtml(riderPhone),
                escapeHtml(vehicle),
                formattedTime,
                escapeHtml(event.getDeliveryAddress() != null ? event.getDeliveryAddress() : "Your destination")
        );
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
