package com.woopaca.likeknu.entity;

import com.woopaca.likeknu.Campus;
import com.woopaca.likeknu.Tag;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Table(name = "device")
@Entity
public class Device {

    @Id
    private String id;

    @Setter
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Campus campus;

    @Column(name = "notification", nullable = false)
    private boolean isTurnOnNotification;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    private String platform;

    private String modelName;

    private String osVersion;

    private String appVersion;

    private String themeColor;

    private String favoriteCafeteria;

    private String expoPushToken;

    private LocalDateTime lastVisitedAt;

    @JoinTable(name = "device_bookmark",
            joinColumns = @JoinColumn(name = "device"),
            inverseJoinColumns = @JoinColumn(name = "announcement"))
    @ManyToMany
    private Set<Announcement> bookmarks = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    @CollectionTable(name = "subscribe", joinColumns = @JoinColumn(name = "device_id"))
    @Column(name = "tag")
    @ElementCollection(fetch = FetchType.LAZY)
    private List<Tag> subscribeTags = new ArrayList<>();

    protected Device() {
    }

    @Builder
    public Device(String id, Campus campus, LocalDateTime registeredAt, String appVersion, String themeColor, String favoriteCafeteria, String expoPushToken) {
        this.id = id;
        this.expoPushToken = expoPushToken;
        this.campus = campus;
        this.registeredAt = registeredAt;
        this.appVersion = appVersion;
        this.themeColor = themeColor;
        this.favoriteCafeteria = favoriteCafeteria;
    }

    public void updateSubscribesTags(List<Tag> tags) {
        subscribeTags.clear();
        subscribeTags.addAll(tags);
    }

    public void updateNotification(boolean isTurnOnNotification) {
        this.isTurnOnNotification = isTurnOnNotification;
    }

    public void updateExpoPushToken(String expoPushToken) {
        this.expoPushToken = expoPushToken;
    }

    public void update(String platform, Campus campus, String themeColor, String favoriteCafeteria, String modelName,
                       String osVersion, String appVersion, String expoPushToken) {
        this.platform = platform;
        this.campus = campus;
        this.themeColor = themeColor;
        this.favoriteCafeteria = favoriteCafeteria;
        lastVisitedAt = LocalDateTime.now();

        if (modelName != null) {
            this.modelName = modelName;
        }
        if (osVersion != null) {
            this.osVersion = osVersion;
        }
        if (appVersion != null) {
            this.appVersion = appVersion;
        }
        if (expoPushToken != null) {
            this.expoPushToken = expoPushToken;
        }
    }
}
