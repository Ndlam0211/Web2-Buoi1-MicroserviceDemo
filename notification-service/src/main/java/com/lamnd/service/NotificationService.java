package com.lamnd.service;

import com.lamnd.enums.NotificationMethod;

public interface NotificationService<T> {
    NotificationMethod getNotificationMethod();
    void sendMessage(T message);
}
