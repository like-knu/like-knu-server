package com.woopaca.likeknu.entity;

import java.io.Serializable;
import java.util.Objects;

public class DeviceNotificationId implements Serializable {

    private String device;
    private String notification;

    public DeviceNotificationId() {
    }

    public DeviceNotificationId(String device, String notification) {
        this.device = device;
        this.notification = notification;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof DeviceNotificationId that))
            return false;
        return Objects.equals(device, that.device) && Objects.equals(notification, that.notification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, notification);
    }
}
