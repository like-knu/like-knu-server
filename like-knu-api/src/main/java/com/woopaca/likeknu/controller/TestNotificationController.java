package com.woopaca.likeknu.controller;

import com.woopaca.likeknu.Campus;
import com.woopaca.likeknu.Category;
import com.woopaca.likeknu.controller.dto.base.ResponseDto;
import com.woopaca.likeknu.job.announcement.dto.AnnouncementMessage;
import com.woopaca.likeknu.job.announcement.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Profile({"local", "local-dev"})
@RestController
@RequestMapping("/api/test")
public class TestNotificationController {

    private final AnnouncementService announcementService;

    public TestNotificationController(@Qualifier("jobAnnouncementService") AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping("/announcements")
    public ResponseDto<String> publishFakeAnnouncement(
            @RequestParam("title") String title,
            @RequestParam(name = "campus", defaultValue = "SINGWAN") Campus campus,
            @RequestParam(name = "category", defaultValue = "STUDENT_NEWS") Category category
    ) {
        long now = System.currentTimeMillis();
        AnnouncementMessage message = AnnouncementMessage.builder()
                .title(title)
                .announcementUrl("https://test.local/announcement/" + now)
                .simpleUrl("https://test.local/announcement/" + now)
                .announcementDate(LocalDate.now())
                .campus(campus)
                .category(category)
                .build();
        announcementService.updateAnnouncements(List.of(message));
        return ResponseDto.of("Test announcement published. title=" + title);
    }
}
