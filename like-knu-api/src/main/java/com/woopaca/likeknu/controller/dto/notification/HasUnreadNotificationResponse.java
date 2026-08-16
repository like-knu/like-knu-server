package com.woopaca.likeknu.controller.dto.notification;

public record HasUnreadNotificationResponse(boolean hasUnread) {

    public static HasUnreadNotificationResponse of(boolean hasUnread) {
        return new HasUnreadNotificationResponse(hasUnread);
    }
}
