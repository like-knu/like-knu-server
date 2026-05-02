package com.woopaca.likeknu.repository;

import com.woopaca.likeknu.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {
}
