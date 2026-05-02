package com.woopaca.likeknu.repository;

import com.woopaca.likeknu.Campus;
import com.woopaca.likeknu.entity.Device;
import com.woopaca.likeknu.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, String> {

    List<Keyword> findAllByDeviceOrderByCreatedAtAsc(Device device);

    int countByDevice(Device device);

    Optional<Keyword> findByDeviceAndKeyword(Device device, String keyword);

    @Query("""
            SELECT DISTINCT k.device
            FROM Keyword k
            WHERE LOCATE(k.keyword, LOWER(:title)) > 0
              AND (:campus = com.woopaca.likeknu.Campus.ALL OR k.device.campus = :campus)
            """)
    List<Device> findDevicesMatching(@Param("title") String title, @Param("campus") Campus campus);
}
