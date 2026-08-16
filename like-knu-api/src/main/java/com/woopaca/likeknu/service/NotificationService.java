package com.woopaca.likeknu.service;

import com.woopaca.likeknu.controller.dto.base.PageDto;
import com.woopaca.likeknu.controller.dto.notification.NotificationListResponse;
import com.woopaca.likeknu.entity.Device;
import com.woopaca.likeknu.entity.DeviceNotification;
import com.woopaca.likeknu.entity.Notification;
import com.woopaca.likeknu.exception.BusinessException;
import com.woopaca.likeknu.repository.DeviceNotificationRepository;
import com.woopaca.likeknu.repository.DeviceRepository;
import com.woopaca.likeknu.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Transactional
@Service
public class NotificationService {

    private static final int DEFAULT_NOTIFICATION_PAGE_SIZE = 100;

    private final DeviceRepository deviceRepository;
    private final NotificationRepository notificationRepository;
    private final DeviceNotificationRepository deviceNotificationRepository;

    public NotificationService(DeviceRepository deviceRepository,
                               NotificationRepository notificationRepository,
                               DeviceNotificationRepository deviceNotificationRepository) {
        this.deviceRepository = deviceRepository;
        this.notificationRepository = notificationRepository;
        this.deviceNotificationRepository = deviceNotificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationListResponse> getNotificationList(String deviceId, Period period, PageDto pageDto) {
        LocalDateTime fromDate = LocalDate.now().minus(period).atStartOfDay();
        PageRequest pageRequest = PageRequest.of(pageDto.getCurrentPage() - 1, DEFAULT_NOTIFICATION_PAGE_SIZE,
                Sort.by(Sort.Order.desc("notification.notificationDate")));
        Device device = findDevice(deviceId);

        Page<DeviceNotification> deviceNotifications = deviceNotificationRepository
                .findByDeviceAndNotificationDateAfter(device, fromDate, pageRequest);
        pageDto.updateTotalPages(deviceNotifications.getTotalPages());
        return deviceNotifications.stream()
                .map(NotificationListResponse::of)
                .toList();
    }

    public void markAsRead(String deviceId, String notificationId) {
        Device device = findDevice(deviceId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(String.format("Notification not found! [%s]", notificationId)));
        DeviceNotification deviceNotification = deviceNotificationRepository
                .findByDeviceAndNotification(device, notification)
                .orElseThrow(() -> new BusinessException("이 알림에 접근할 수 없어요."));
        deviceNotification.markAsRead();
    }

    public void markAllAsRead(String deviceId) {
        Device device = findDevice(deviceId);
        deviceNotificationRepository.markAllAsReadByDevice(device);
    }

    @Transactional(readOnly = true)
    public boolean hasUnread(String deviceId) {
        Device device = findDevice(deviceId);
        return deviceNotificationRepository.existsByDeviceAndReadFalse(device);
    }

    private Device findDevice(String deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(String.format("Device not found! [%s]", deviceId)));
    }
}
