package com.woopaca.likeknu.service.notification.event;

import com.woopaca.likeknu.NotificationType;
import com.woopaca.likeknu.external.expo.ExpoPushMessage;

import java.util.List;

public record PushDispatchEvent(NotificationType type, int recipientCount, List<ExpoPushMessage> messages) {
}
