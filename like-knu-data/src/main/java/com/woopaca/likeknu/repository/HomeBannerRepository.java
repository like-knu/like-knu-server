package com.woopaca.likeknu.repository;

import com.woopaca.likeknu.entity.HomeBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HomeBannerRepository extends JpaRepository<HomeBanner, Long> {

    List<HomeBanner> findAllByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanEqualOrderByCreatedAtDesc(
            LocalDateTime startBound, LocalDateTime endBound);
}
