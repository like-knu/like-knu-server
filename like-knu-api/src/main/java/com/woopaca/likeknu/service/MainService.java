package com.woopaca.likeknu.service;

import com.woopaca.likeknu.Campus;
import com.woopaca.likeknu.Category;
import com.woopaca.likeknu.MealType;
import com.woopaca.likeknu.controller.dto.announcement.MainAnnouncementsResponse;
import com.woopaca.likeknu.controller.dto.banner.HomeBannerResponse;
import com.woopaca.likeknu.controller.dto.menu.MainMenuResponse;
import com.woopaca.likeknu.controller.dto.menu.MainMenuResponseV2;
import com.woopaca.likeknu.controller.dto.schedule.MainScheduleResponse;
import com.woopaca.likeknu.entity.AcademicCalendar;
import com.woopaca.likeknu.entity.Announcement;
import com.woopaca.likeknu.entity.Cafeteria;
import com.woopaca.likeknu.entity.Menu;
import com.woopaca.likeknu.repository.AcademicCalendarRepository;
import com.woopaca.likeknu.repository.AnnouncementRepository;
import com.woopaca.likeknu.repository.CafeteriaRepository;
import com.woopaca.likeknu.repository.HomeBannerRepository;
import com.woopaca.likeknu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainService {

    private final AnnouncementRepository announcementRepository;
    private final MenuRepository menuRepository;
    private final CafeteriaRepository cafeteriaRepository;
    private final AcademicCalendarRepository academicCalendarRepository;
    private final HomeBannerRepository homeBannerRepository;

    public List<MainAnnouncementsResponse> getAnnouncementsResponse(Campus campus) {
        List<Campus> campusList = List.of(Campus.ALL, campus);

        Sort sort = Sort.by(Order.desc("announcementDate"), Order.desc("collectedAt"));
        List<Announcement> getAnnouncements =
                announcementRepository.findTop4ByCampusInAndCategory(campusList, Category.STUDENT_NEWS, sort);

        return getAnnouncements.stream()
                .map(MainAnnouncementsResponse::of)
                .collect(Collectors.toList());
    }

    public List<MainMenuResponse> getMenuResponse(Campus campus) {
        return cafeteriaRepository.findByCampus(campus)
                .stream()
                .sorted(Comparator.comparing(Cafeteria::getSequence))
                .map(cafeteria -> findNowMealTypeMenu(cafeteria)
                        .map(menu -> MainMenuResponse.of(cafeteria, menu.getMenus()))
                        .orElse(MainMenuResponse.empty(cafeteria)))
                .toList();
    }

    public List<MainMenuResponseV2> getMenuResponseV2(Campus campus) {
        return cafeteriaRepository.findByCampus(campus)
                .stream()
                .sorted(Comparator.comparing(Cafeteria::getSequence))
                .map(cafeteria -> findNowMealTypeMenu(cafeteria)
                        .map(menu -> MainMenuResponseV2.of(cafeteria, menu.getMenus()))
                        .orElse(MainMenuResponseV2.empty(cafeteria)))
                .toList();
    }

    private Optional<Menu> findNowMealTypeMenu(Cafeteria cafeteria) {
        return menuRepository.findByCafeteriaAndMenuDate(cafeteria, LocalDate.now())
                .stream()
                .filter(menu -> menu.getMealType().equals(MealType.now()))
                .findAny();
    }

    public List<MainScheduleResponse> getScheduleResponse() {
        LocalDate currentDate = LocalDate.now();
        List<AcademicCalendar> calendarList = academicCalendarRepository
                .findBetweenDateLimit4(currentDate, currentDate.plusWeeks(4));

        return calendarList.stream()
                .map(MainScheduleResponse::of)
                .collect(Collectors.toList());
    }

    public List<HomeBannerResponse> getActiveHomeBanners() {
        LocalDateTime now = LocalDateTime.now();
        return homeBannerRepository
                .findAllByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanEqualOrderByCreatedAtDesc(now, now)
                .stream()
                .map(HomeBannerResponse::of)
                .collect(Collectors.toList());
    }
}
