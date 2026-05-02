package com.woopaca.likeknu.repository;

import com.woopaca.likeknu.entity.Device;
import com.woopaca.likeknu.entity.Menu;
import com.woopaca.likeknu.entity.MenuRating;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRatingRepository extends JpaRepository<MenuRating, Long> {

    @EntityGraph(attributePaths = "device")
    List<MenuRating> findByMenu(Menu menu);

    Optional<MenuRating> findByMenuAndDevice(Menu menu, Device device);
}
