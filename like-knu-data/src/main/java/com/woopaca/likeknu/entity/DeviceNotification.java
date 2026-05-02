package com.woopaca.likeknu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Table(name = "device_notification")
@IdClass(DeviceNotificationId.class)
@Entity
public class DeviceNotification {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "`read`", nullable = false)
    private boolean read;

    protected DeviceNotification() {
    }

    private DeviceNotification(Device device, Notification notification) {
        this.device = device;
        this.notification = notification;
        this.read = false;
    }

    public static DeviceNotification of(Device device, Notification notification) {
        return new DeviceNotification(device, notification);
    }

    public void markAsRead() {
        this.read = true;
    }
}
