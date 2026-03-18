package com.woopaca.likeknu.collector.announcement.dto;

import com.woopaca.likeknu.Category;
import com.woopaca.likeknu.collector.menu.constants.Campus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record Announcement(String title, String announcementUrl, String simpleUrl, LocalDate announcementDate,
                           Campus campus,
                           Category category) {

    public static Announcement ofDormitory(String title, String url, LocalDate date, Campus campus) {
        return ofCategory(title, url, url, date, campus, Category.DORMITORY);
    }

    public static Announcement ofLibrary(String title, String url, LocalDate date, Campus campus) {
        return ofCategory(title, url, url, date, campus, Category.LIBRARY);
    }

    public static Announcement ofStudentNews(String title, String url, String simpleUrl, LocalDate date, Campus campus) {
        return ofCategory(title, url, simpleUrl, date, campus, Category.STUDENT_NEWS);
    }

    public static Announcement ofRecruitment(String title, String url, String simpleUrl, LocalDate date, Campus campus) {
        return ofCategory(title, url, simpleUrl, date, campus, Category.RECRUITMENT);
    }

    private static Announcement ofCategory(String title, String url, String simpleUrl, LocalDate date, Campus campus, Category category) {
        return Announcement.builder()
                .title(title)
                .announcementUrl(url)
                .simpleUrl(simpleUrl)
                .announcementDate(date)
                .campus(campus)
                .category(category)
                .build();
    }
}
