package com.woopaca.likeknu.entity;

import com.woopaca.likeknu.Domain;
import com.woopaca.likeknu.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Table(name = "notification")
@Entity
public class Notification {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false)
    private String notificationTitle;

    @Column(nullable = false)
    private String notificationBody;

    @Column(nullable = false)
    private LocalDateTime notificationDate;

    @Column(nullable = false)
    private String notificationUrl;

    protected Notification() {
    }

    @Builder
    public Notification(NotificationType type, String notificationTitle, String notificationBody,
                        LocalDateTime notificationDate, String notificationUrl) {
        this.id = String.join("", Domain.NOTIFICATION.toString().toLowerCase(), "_",
                UUID.randomUUID().toString().replace("-", ""));
        this.type = type;
        this.notificationTitle = notificationTitle;
        this.notificationBody = notificationBody;
        this.notificationDate = notificationDate;
        this.notificationUrl = notificationUrl;
    }

    public static Notification create(NotificationType type, String title, String body, String url) {
        return Notification.builder()
                .type(type)
                .notificationTitle(title)
                .notificationBody(body)
                .notificationDate(LocalDateTime.now())
                .notificationUrl(url == null ? "" : url)
                .build();
    }
}
