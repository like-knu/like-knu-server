package com.woopaca.likeknu.job.announcement.event;

import com.woopaca.likeknu.entity.Announcement;

import java.util.List;

public record NewAnnouncementsSavedEvent(List<Announcement> announcements) {
}
