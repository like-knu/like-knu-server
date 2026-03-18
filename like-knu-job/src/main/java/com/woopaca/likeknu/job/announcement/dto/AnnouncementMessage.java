package com.woopaca.likeknu.job.announcement.dto;

import com.woopaca.likeknu.Campus;
import com.woopaca.likeknu.Category;
import com.woopaca.likeknu.Tag;
import com.woopaca.likeknu.entity.Announcement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@ToString
public class AnnouncementMessage {

    @NotBlank
    private String title;

    @NotBlank
    private String announcementUrl;

    private String simpleUrl;

    @NotNull
    private LocalDate announcementDate;

    @NotNull
    private Campus campus;

    @NotNull
    private Category category;

    protected AnnouncementMessage() {
    }

    @Builder
    public AnnouncementMessage(String title, String announcementUrl, String simpleUrl, LocalDate announcementDate, Campus campus, Category category) {
        this.title = title;
        this.announcementUrl = announcementUrl;
        this.simpleUrl = simpleUrl;
        this.announcementDate = announcementDate;
        this.campus = campus;
        this.category = category;
    }

    public static AnnouncementMessage of(Announcement announcement) {
        return AnnouncementMessage.builder()
                .title(announcement.getAnnouncementTitle())
                .announcementUrl(announcement.getAnnouncementUrl())
                .simpleUrl(announcement.getAnnouncementUrl())
                .announcementDate(announcement.getAnnouncementDate())
                .campus(announcement.getCampus())
                .category(announcement.getCategory())
                .build();
    }

    public Announcement toEntity(Tag tag) {
        return Announcement.builder()
                .announcementTitle(title)
                .announcementUrl(announcementUrl)
                .simpleUrl(simpleUrl)
                .announcementDate(announcementDate)
                .campus(campus)
                .category(category)
                .tag(tag)
                .build();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;

        AnnouncementMessage that = (AnnouncementMessage) object;

        if (!Objects.equals(title, that.title))
            return false;
        if (!Objects.equals(announcementUrl, that.announcementUrl))
            return false;
        if (!Objects.equals(announcementDate, that.announcementDate))
            return false;
        if (campus != that.campus)
            return false;
        return category == that.category;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (announcementUrl != null ? announcementUrl.hashCode() : 0);
        result = 31 * result + (announcementDate != null ? announcementDate.hashCode() : 0);
        result = 31 * result + (campus != null ? campus.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }
}
