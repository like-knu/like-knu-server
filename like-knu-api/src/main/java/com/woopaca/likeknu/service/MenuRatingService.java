package com.woopaca.likeknu.service;

import com.woopaca.likeknu.controller.dto.menu.MenuRatingStatusResponse;
import com.woopaca.likeknu.entity.Device;
import com.woopaca.likeknu.entity.Menu;
import com.woopaca.likeknu.entity.MenuRating;
import com.woopaca.likeknu.exception.BusinessException;
import com.woopaca.likeknu.repository.DeviceRepository;
import com.woopaca.likeknu.repository.MenuRatingRepository;
import com.woopaca.likeknu.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MenuRatingService {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final MenuRatingRepository menuRatingRepository;
    private final DeviceRepository deviceRepository;
    private final MenuRepository menuRepository;

    public MenuRatingService(MenuRatingRepository menuRatingRepository, DeviceRepository deviceRepository,
                             MenuRepository menuRepository) {
        this.menuRatingRepository = menuRatingRepository;
        this.deviceRepository = deviceRepository;
        this.menuRepository = menuRepository;
    }

    public MenuRatingStatusResponse getMenuRatingStatus(String menuId, String deviceId) {
        Device device = findDevice(deviceId);
        Menu menu = findMenu(menuId);

        List<MenuRating> ratings = menuRatingRepository.findByMenu(menu);
        double averageRating = ratings.stream()
                .mapToInt(MenuRating::getRating)
                .average()
                .orElse(0.0);
        Integer ownRating = ratings.stream()
                .filter(rating -> rating.getDevice().equals(device))
                .findAny()
                .map(MenuRating::getRating)
                .orElse(null);
        return new MenuRatingStatusResponse(averageRating, ratings.size(), ownRating);
    }

    @Transactional
    public MenuRatingStatusResponse updateRating(String menuId, String deviceId, int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessException(String.format("rating must be between %d and %d", MIN_RATING, MAX_RATING));
        }

        Device device = findDevice(deviceId);
        Menu menu = findMenu(menuId);

        Optional<MenuRating> ownRating = menuRatingRepository.findByMenuAndDevice(menu, device);
        ownRating.ifPresentOrElse(existing -> toggleOrChange(existing, rating),
                () -> menuRatingRepository.save(MenuRating.builder()
                        .rating(rating)
                        .menu(menu)
                        .device(device)
                        .build()));
        return getMenuRatingStatus(menuId, deviceId);
    }

    private void toggleOrChange(MenuRating existing, int rating) {
        if (existing.getRating() == rating) {
            menuRatingRepository.delete(existing);
            return;
        }
        existing.changeRating(rating);
    }

    private Device findDevice(String deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(String.format("device does not exist [%s]", deviceId)));
    }

    private Menu findMenu(String menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(String.format("menu does not exist [%s]", menuId)));
    }
}
