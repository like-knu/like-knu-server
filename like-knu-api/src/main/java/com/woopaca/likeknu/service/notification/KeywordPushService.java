package com.woopaca.likeknu.service.notification;

import com.woopaca.likeknu.NotificationType;
import com.woopaca.likeknu.entity.Announcement;
import com.woopaca.likeknu.entity.Device;
import com.woopaca.likeknu.job.announcement.event.NewAnnouncementsSavedEvent;
import com.woopaca.likeknu.repository.KeywordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
public class KeywordPushService {

    private static final String KEYWORD_NOTIFICATION_TITLE = "새 공지가 등록되었습니다";

    private final KeywordRepository keywordRepository;
    private final PushNotificationService pushNotificationService;

    public KeywordPushService(KeywordRepository keywordRepository,
                              PushNotificationService pushNotificationService) {
        this.keywordRepository = keywordRepository;
        this.pushNotificationService = pushNotificationService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void onNewAnnouncementsSaved(NewAnnouncementsSavedEvent event) {
        for (Announcement announcement : event.announcements()) {
            try {
                List<Device> matched = keywordRepository.findDevicesMatching(
                        announcement.getAnnouncementTitle(), announcement.getCampus());
                if (matched.isEmpty()) {
                    continue;
                }
                pushNotificationService.publish(
                        NotificationType.KEYWORD_MATCH,
                        KEYWORD_NOTIFICATION_TITLE,
                        announcement.getAnnouncementTitle(),
                        announcement.getAnnouncementUrl(),
                        matched
                );
            } catch (Exception e) {
                log.error("[KeywordPushService] 키워드 푸시 처리 실패. announcementId: {}",
                        announcement.getId(), e);
            }
        }
    }
}
