package com.woopaca.likeknu.service.notification;

import com.woopaca.likeknu.external.expo.ExpoPushClient;
import com.woopaca.likeknu.service.notification.event.PushDispatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class PushDispatcher {

    private final ExpoPushClient expoPushClient;

    public PushDispatcher(ExpoPushClient expoPushClient) {
        this.expoPushClient = expoPushClient;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onPushDispatch(PushDispatchEvent event) {
        expoPushClient.sendAsync(event.messages());
        log.info("[PushDispatcher] 푸시 발송. type: {}, recipients: {}", event.type(), event.recipientCount());
    }
}
