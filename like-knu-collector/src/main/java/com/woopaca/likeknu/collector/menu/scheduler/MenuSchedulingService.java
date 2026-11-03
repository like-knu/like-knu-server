package com.woopaca.likeknu.collector.menu.scheduler;

import com.woopaca.likeknu.collector.menu.MenuCollector;
import com.woopaca.likeknu.collector.menu.dto.Meal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MenuSchedulingService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final List<MenuCollector> menuCollectors;

    public MenuSchedulingService(List<MenuCollector> menuCollectors, ApplicationEventPublisher applicationEventPublisher) {
        this.menuCollectors = menuCollectors;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Scheduled(cron = "50 0/10 9-18 * * *")
    public void scheduleMenuProduce() {
        menuCollectors.forEach(this::produceMenus);
    }

    private void produceMenus(MenuCollector menuCollector) {
        try {
            menuCollector.collectMenus()
                    .forEach(applicationEventPublisher::publishEvent);
        } catch (RuntimeException e) {
            log.error("식단 수집 실패. collector = {}", menuCollector.getClass().getSimpleName(), e);
        }
    }
}
