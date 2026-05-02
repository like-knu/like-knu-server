package com.woopaca.likeknu.repository;

import com.woopaca.likeknu.entity.Device;
import com.woopaca.likeknu.entity.DeviceNotification;
import com.woopaca.likeknu.entity.DeviceNotificationId;
import com.woopaca.likeknu.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeviceNotificationRepository extends JpaRepository<DeviceNotification, DeviceNotificationId> {

    @Query("""
            SELECT dn
            FROM DeviceNotification dn
            JOIN FETCH dn.notification n
            WHERE dn.device = :device AND n.notificationDate >= :fromDate
            """)
    Page<DeviceNotification> findByDeviceAndNotificationDateAfter(@Param("device") Device device,
                                                                  @Param("fromDate") LocalDateTime fromDate,
                                                                  Pageable pageable);

    Optional<DeviceNotification> findByDeviceAndNotification(Device device, Notification notification);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE DeviceNotification dn
            SET dn.read = true
            WHERE dn.device = :device AND dn.read = false
            """)
    int markAllAsReadByDevice(@Param("device") Device device);
}
