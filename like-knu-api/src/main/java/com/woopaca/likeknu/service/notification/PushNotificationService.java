package com.woopaca.likeknu.service.notification;

import com.woopaca.likeknu.NotificationType;
import com.woopaca.likeknu.entity.Device;
import com.woopaca.likeknu.entity.DeviceNotification;
import com.woopaca.likeknu.entity.Notification;
import com.woopaca.likeknu.external.expo.ExpoPushMessage;
import com.woopaca.likeknu.repository.DeviceNotificationRepository;
import com.woopaca.likeknu.repository.NotificationRepository;
import com.woopaca.likeknu.service.notification.event.PushDispatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 알림 도메인을 가리지 않는 공통 푸시 발송 서비스.
 * 새로운 알림 종류(셔틀, 식당 등)가 추가되면 도메인 서비스가 이 publish 메서드만 호출하면 된다.
 */
@Slf4j
@Service
public class PushNotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceNotificationRepository deviceNotificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PushNotificationService(NotificationRepository notificationRepository,
                                   DeviceNotificationRepository deviceNotificationRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.notificationRepository = notificationRepository;
        this.deviceNotificationRepository = deviceNotificationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void publish(NotificationType type, String title, String body, String url, List<Device> targets) {
        List<Device> subscribers = targets.stream()
                .filter(Device::isTurnOnNotification)
                .toList();
        if (subscribers.isEmpty()) {
            return;
        }

        Notification notification = Notification.create(type, title, body, url);
        notificationRepository.save(notification);

        List<DeviceNotification> deviceNotifications = subscribers.stream()
                .map(device -> DeviceNotification.of(device, notification))
                .toList();
        deviceNotificationRepository.saveAll(deviceNotifications);

        List<ExpoPushMessage> messages = subscribers.stream()
                .filter(device -> device.getExpoPushToken() != null && !device.getExpoPushToken().isBlank())
                .map(device -> ExpoPushMessage.of(device.getExpoPushToken(), title, body, Map.of(
                        "type", type.name(),
                        "url", url == null ? "" : url,
                        "notificationId", notification.getId()
                )))
                .toList();
        if (!messages.isEmpty()) {
            eventPublisher.publishEvent(new PushDispatchEvent(type, messages.size(), messages));
        }
    }
}
