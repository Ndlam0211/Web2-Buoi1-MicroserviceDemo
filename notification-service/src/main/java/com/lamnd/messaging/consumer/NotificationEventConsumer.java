package com.lamnd.messaging.consumer;

import com.lamnd.enums.NotificationMethod;
import com.lamnd.factory.NotificationServiceFactory;
import com.lamnd.messaging.event.OrderPlacedEvent;
import com.lamnd.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationServiceFactory notificationServiceFactory;

    @RabbitListener(queues = "notification-queue")
    public void bookingListener(OrderPlacedEvent payment) {
        NotificationService<OrderPlacedEvent> notificationService = (NotificationService<OrderPlacedEvent>) notificationServiceFactory
                .getNotificationService(NotificationMethod.EMAIL);

        notificationService.sendMessage(payment);
        System.out.println("Send notification successfully");
    }
}