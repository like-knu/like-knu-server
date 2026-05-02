package com.woopaca.likeknu.job.announcement;

import com.woopaca.likeknu.Tag;
import com.woopaca.likeknu.entity.Announcement;
import com.woopaca.likeknu.job.announcement.dto.AnnouncementMessage;
import com.woopaca.likeknu.repository.AnnouncementRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AnnouncementModifier {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementTagAbstracter announcementTagAbstracter;

    public AnnouncementModifier(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
        this.announcementTagAbstracter = announcementMessage -> Tag.of(announcementMessage.getCategory().getCategoryName());
    }

    public Optional<Announcement> appendAnnouncement(AnnouncementMessage announcementMessage) {
        Optional<Announcement> existing = announcementRepository.findByAnnouncementUrl(announcementMessage.getAnnouncementUrl());
        if (existing.isPresent()) {
            existing.get().update(announcementMessage.getTitle(), announcementMessage.getCampus());
            return Optional.empty();
        }
        Tag tag = announcementTagAbstracter.abstractTag(announcementMessage);
        Announcement announcement = announcementMessage.toEntity(tag);
        return Optional.of(announcementRepository.save(announcement));
    }
}
