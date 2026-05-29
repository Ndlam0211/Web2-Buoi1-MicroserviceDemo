package com.lamnd.factory;

import com.lamnd.enums.NotificationMethod;
import com.lamnd.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class NotificationServiceFactory {

    private final Map<NotificationMethod, NotificationService<?>> serviceMap;

    public NotificationServiceFactory(List<NotificationService<?>> notificationServices) {
        if (notificationServices == null || notificationServices.isEmpty()) {
            log.error("No notification services found");
            throw new IllegalArgumentException("At least one notification service must be provided");
        }

        this.serviceMap = notificationServices.stream()
                .collect(Collectors.toMap(
                        NotificationService::getNotificationMethod,
                        service -> service
                ));
    }

    public NotificationService<?> getNotificationService(NotificationMethod method) {
        if (method == null) {
            log.error("Notification method is null");
            throw new IllegalArgumentException("Notification method cannot be null");
        }

        NotificationService<?> service = serviceMap.get(method);

        if (service == null) {
            log.error("No notification service found for method: {}", method);
            throw new RuntimeException("Unsupported notification method: " + method);
        }

        return service;
    }
}
