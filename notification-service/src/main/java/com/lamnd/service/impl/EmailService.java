package com.lamnd.service.impl;

import com.lamnd.enums.NotificationMethod;
import com.lamnd.messaging.event.OrderPlacedEvent;
import com.lamnd.service.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService implements NotificationService<OrderPlacedEvent> {

    private final JavaMailSender javaMailSender;

    @Override
    public NotificationMethod getNotificationMethod() {
        return NotificationMethod.EMAIL;
    }

    @Override
    public void sendMessage(OrderPlacedEvent message) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("hello@demomailtrap.co");
            helper.setTo(message.getEmail());
            helper.setSubject("Đặt hàng thành công #"+ message.getOrderId());

            String body = "<p>Xin chào Userid " + message.getUserId() + ",</p>" +
                    "<p>Cảm ơn bạn đã đặt hàng. Đơn hàng của bạn đã được nhận và đang được xử lý.</p>" +
                    "<p>Chi tiết đơn hàng:</p>" +
                    "<ul>" +
                    "<li>Mã đơn hàng: " + message.getOrderId() + "</li>" +
                    "<li>Tổng tiền: " + message.getTotal() + "</li>" +
                    "</ul>" +
                    "<p>Chúng tôi sẽ thông báo cho bạn khi đơn hàng được giao.</p>" +
                    "<p>Trân trọng,</p>" +
                    "<p>Đội ngũ bán hàng</p>";

            helper.setText(body, true);

            javaMailSender.send(mimeMessage);
            System.out.println("Gui email thanh cong cho user: "+ message.getEmail());
        } catch (MessagingException e) {
            System.out.println("Gui email that bai: "+ e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
