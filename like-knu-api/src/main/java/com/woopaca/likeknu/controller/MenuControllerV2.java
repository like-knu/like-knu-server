package com.woopaca.likeknu.controller;

import com.woopaca.likeknu.Campus;
import com.woopaca.likeknu.controller.dto.base.ResponseDto;
import com.woopaca.likeknu.controller.dto.menu.CafeteriaListResponse;
import com.woopaca.likeknu.controller.dto.menu.CafeteriaMealListResponse;
import com.woopaca.likeknu.controller.dto.menu.MenuRatingRequest;
import com.woopaca.likeknu.controller.dto.menu.MenuRatingStatusResponse;
import com.woopaca.likeknu.entity.Cafeteria;
import com.woopaca.likeknu.repository.CafeteriaRepository;
import com.woopaca.likeknu.service.MenuRatingService;
import com.woopaca.likeknu.service.MenuService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v2/menus")
public class MenuControllerV2 {

    private final CafeteriaRepository cafeteriaRepository;
    private final MenuService menuService;
    private final MenuRatingService menuRatingService;

    public MenuControllerV2(CafeteriaRepository cafeteriaRepository, MenuService menuService,
                            MenuRatingService menuRatingService) {
        this.cafeteriaRepository = cafeteriaRepository;
        this.menuService = menuService;
        this.menuRatingService = menuRatingService;
    }

    @GetMapping("/cafeterias")
    public ResponseDto<List<CafeteriaListResponse>> getCafeteriasByCampus(@RequestParam("campus") Campus campus) {
        List<CafeteriaListResponse> cafeterias = cafeteriaRepository.findByCampus(campus)
                .stream()
                .sorted(Comparator.comparing(Cafeteria::getSequence))
                .map(CafeteriaListResponse::from)
                .toList();
        return ResponseDto.of(cafeterias);
    }

    @GetMapping
    public ResponseDto<List<CafeteriaMealListResponse>> getMenuByCampus(
            @RequestParam("cafeteriaId") String cafeteriaId
    ) {
        List<CafeteriaMealListResponse> cafeteriaMeals = menuService.getCafeteriaMealsV2(cafeteriaId);
        return ResponseDto.of(cafeteriaMeals);
    }

    @GetMapping("/{menuId}/rating")
    public ResponseDto<MenuRatingStatusResponse> getRatingStatus(
            @PathVariable("menuId") String menuId,
            @RequestParam("deviceId") String deviceId
    ) {
        return ResponseDto.of(menuRatingService.getMenuRatingStatus(menuId, deviceId));
    }

    @PutMapping("/{menuId}/rating")
    public ResponseDto<MenuRatingStatusResponse> updateRating(
            @PathVariable("menuId") String menuId,
            @RequestBody MenuRatingRequest request
    ) {
        try {
            return ResponseDto.of(menuRatingService.updateRating(menuId, request.deviceId(), request.rating()));
        } catch (DataIntegrityViolationException e) {
            return ResponseDto.of(menuRatingService.updateRating(menuId, request.deviceId(), request.rating()));
        }
    }
}
