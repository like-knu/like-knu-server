package com.woopaca.likeknu.controller.dto.notification;

import com.woopaca.likeknu.NotificationType;
import com.woopaca.likeknu.entity.DeviceNotification;
import com.woopaca.likeknu.entity.Notification;
import lombok.Builder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record NotificationListResponse(String notificationId, NotificationType type, String notificationTitle,
                                       String notificationBody, String notificationDate, String notificationUrl,
                                       boolean read) {

    @Builder
    public NotificationListResponse(String notificationId, NotificationType type, String notificationTitle, String notificationBody, String notificationDate, String notificationUrl, boolean read) {
        this.notificationId = notificationId;
        this.type = type;
        this.notificationTitle = notificationTitle;
        this.notificationBody = notificationBody;
        this.notificationDate = notificationDate;
        this.notificationUrl = notificationUrl;
        this.read = read;
    }

    public static NotificationListResponse of(DeviceNotification deviceNotification) {
        Notification notification = deviceNotification.getNotification();
        LocalDateTime date = notification.getNotificationDate();
        Duration duration = Duration.between(date, LocalDateTime.now());
        String notificationDate = getFormattedNotificationDate(duration, date);
        return NotificationListResponse.builder()
                .notificationId(notification.getId())
                .type(notification.getType())
                .notificationTitle(notification.getNotificationTitle())
                .notificationBody(notification.getNotificationBody())
                .notificationDate(notificationDate)
                .notificationUrl(notification.getNotificationUrl())
                .read(deviceNotification.isRead())
                .build();
    }

    private static String getFormattedNotificationDate(Duration duration, LocalDateTime date) {
        long seconds = duration.toSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        if (seconds < 60) {
            return seconds + "초 전";
        }
        if (minutes < 60) {
            return minutes + "분 전";
        }
        if (hours < 24) {
            return hours + "시간 전";
        }
        if (days <= 10) {
            return days + "일 전";
        }
        return date.format(DateTimeFormatter.ofPattern("M월 d일"));
    }
}
