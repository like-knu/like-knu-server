package com.woopaca.likeknu.job.announcement.service;

import com.woopaca.likeknu.entity.Announcement;
import com.woopaca.likeknu.job.announcement.AnnouncementCache;
import com.woopaca.likeknu.job.announcement.AnnouncementModifier;
import com.woopaca.likeknu.job.announcement.dto.AnnouncementMessage;
import com.woopaca.likeknu.job.announcement.event.NewAnnouncementsSavedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("jobAnnouncementService")
public class AnnouncementService {

    private final AnnouncementCache announcementCache;
    private final AnnouncementModifier announcementModifier;
    private final ApplicationEventPublisher eventPublisher;

    public AnnouncementService(AnnouncementCache announcementCache, AnnouncementModifier announcementModifier,
                               ApplicationEventPublisher eventPublisher) {
        this.announcementCache = announcementCache;
        this.announcementModifier = announcementModifier;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void updateAnnouncements(List<AnnouncementMessage> announcementMessages) {
        List<Announcement> newAnnouncements = new ArrayList<>();
        announcementMessages.stream()
                .filter(announcementCache::isAbsent)
                .forEach(announcementMessage -> {
                    try {
                        announcementModifier.appendAnnouncement(announcementMessage)
                                .ifPresent(newAnnouncements::add);
                        announcementCache.cache(announcementMessage);
                    } catch (Exception e) {
                        log.error("[AnnouncementService] 공지사항 저장 실패. announcement: {}", announcementMessage, e);
                    }
                });

        if (!newAnnouncements.isEmpty()) {
            eventPublisher.publishEvent(new NewAnnouncementsSavedEvent(newAnnouncements));
        }
    }
}
