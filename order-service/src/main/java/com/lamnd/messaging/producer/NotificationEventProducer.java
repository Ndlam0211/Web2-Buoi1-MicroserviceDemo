package com.lamnd.messaging.producer;

import com.lamnd.messaging.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotificationEvent(OrderPlacedEvent orderPlacedEvent) {

        rabbitTemplate.convertAndSend("notification-queue", orderPlacedEvent);
    }
}
