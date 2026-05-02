package com.woopaca.likeknu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class HomeBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "body", nullable = false, length = 200)
    private String body;

    @Column(name = "link_path", length = 200)
    private String linkPath;

    @Column(name = "min_app_version", length = 20)
    private String minAppVersion;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected HomeBanner() {
    }

    @Builder
    public HomeBanner(String title, String body, String linkPath, String minAppVersion,
                      LocalDateTime startsAt, LocalDateTime endsAt, boolean active) {
        this.title = title;
        this.body = body;
        this.linkPath = linkPath;
        this.minAppVersion = minAppVersion;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }
}
