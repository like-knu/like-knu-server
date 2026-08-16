package com.woopaca.likeknu.controller;

import com.woopaca.likeknu.controller.dto.base.PageDto;
import com.woopaca.likeknu.controller.dto.base.PageResponseDto;
import com.woopaca.likeknu.controller.dto.base.ResponseDto;
import com.woopaca.likeknu.controller.dto.notification.HasUnreadNotificationResponse;
import com.woopaca.likeknu.controller.dto.notification.MarkAllAsReadRequest;
import com.woopaca.likeknu.controller.dto.notification.NotificationListResponse;
import com.woopaca.likeknu.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Period;
import java.util.List;

@RequestMapping("/api/notifications")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public PageResponseDto<List<NotificationListResponse>> notificationListWithin30Days(
            @RequestParam(name = "deviceId") String deviceId,
            @RequestParam(name = "page", defaultValue = "1") int page
    ) {
        PageDto pageDto = PageDto.of(page);
        List<NotificationListResponse> notificationList =
                notificationService.getNotificationList(deviceId, Period.ofDays(30), pageDto);
        return PageResponseDto.of(notificationList, pageDto);
    }

    @GetMapping("/unread")
    public ResponseDto<HasUnreadNotificationResponse> hasUnread(@RequestParam("deviceId") String deviceId) {
        boolean hasUnread = notificationService.hasUnread(deviceId);
        return ResponseDto.of(HasUnreadNotificationResponse.of(hasUnread));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseDto<String> markAsRead(
            @PathVariable String notificationId,
            @RequestParam("deviceId") String deviceId
    ) {
        notificationService.markAsRead(deviceId, notificationId);
        return ResponseDto.of("Marked as read.");
    }

    @PutMapping("/read-all")
    public ResponseDto<String> markAllAsRead(@RequestBody MarkAllAsReadRequest request) {
        notificationService.markAllAsRead(request.deviceId());
        return ResponseDto.of("All marked as read.");
    }
}
