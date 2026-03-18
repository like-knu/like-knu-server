package com.woopaca.likeknu.entity;

import com.woopaca.likeknu.Campus;
import com.woopaca.likeknu.Category;
import com.woopaca.likeknu.Domain;
import com.woopaca.likeknu.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
public class Announcement extends BaseEntity {

    @Column(name = "announcement_title", nullable = false)
    private String announcementTitle;

    @Column(name = "announcement_url", nullable = false)
    private String announcementUrl;

    @Column(name = "simple_url")
    private String simpleUrl;

    @Column(name = "announcement_date", nullable = false)
    private LocalDate announcementDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "campus", nullable = false)
    private Campus campus;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false)
    private Tag tag;

    @CreatedDate
    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    protected Announcement() {
        super(Domain.ANNOUNCEMENT);
    }

    @Builder
    public Announcement(String announcementTitle, String announcementUrl, String simpleUrl, LocalDate announcementDate, Campus campus, Category category, Tag tag) {
        this();
        this.announcementTitle = announcementTitle;
        this.announcementUrl = announcementUrl;
        this.simpleUrl = simpleUrl;
        this.announcementDate = announcementDate;
        this.campus = campus;
        this.category = category;
        this.tag = tag;
    }

    public void update(String title, Campus campus) {
        this.announcementTitle = title;
        this.campus = campus;
    }
}
