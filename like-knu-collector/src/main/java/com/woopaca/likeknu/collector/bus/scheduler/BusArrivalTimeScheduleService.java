package com.woopaca.likeknu.collector.bus.scheduler;

import com.woopaca.likeknu.collector.bus.BusArrivalTimeRequestManager;
import com.woopaca.likeknu.collector.bus.DepartureStop;
import com.woopaca.likeknu.collector.bus.MapType;
import com.woopaca.likeknu.collector.bus.dto.BusArrivalTime;
import com.woopaca.likeknu.collector.bus.dto.BusArrivalTimes;
import com.woopaca.likeknu.collector.bus.dto.KakaoRealtimeBusInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Profile("!local")
@Service
public class BusArrivalTimeScheduleService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final BusArrivalTimeRequestManager busArrivalTimeRequestManager;

    public BusArrivalTimeScheduleService(ApplicationEventPublisher applicationEventPublisher, BusArrivalTimeRequestManager busArrivalTimeRequestManager) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.busArrivalTimeRequestManager = busArrivalTimeRequestManager;
    }

    @Scheduled(cron = "*/30 * 6-23 * * *")
    public void schedulingBusArrivalTimeProduce() {
        List<BusArrivalTime> busArrivalTimes = Arrays.stream(DepartureStop.values())
                .flatMap(this::fetchBusArrivalTimes)
                .toList();
        applicationEventPublisher.publishEvent(new BusArrivalTimes(busArrivalTimes));
    }

    private Stream<BusArrivalTime> fetchBusArrivalTimes(DepartureStop departureStop) {
        try {
            if (departureStop.getMapType().equals(MapType.NAVER)) {
                return fetchNaverBusArrivalTimes(departureStop);
            }
            return fetchKakaoBusArrivalTimes(departureStop);
        } catch (HttpStatusCodeException exception) {
            log.error("HTTP 요청 오류 = {}, statusCode = {}", exception.getMessage(), exception.getStatusCode());
            return Stream.empty();
        }
    }

    private Stream<BusArrivalTime> fetchNaverBusArrivalTimes(DepartureStop departureStop) {
        return busArrivalTimeRequestManager.fetchNaverRealTimeBusInformation(departureStop)
                .stream()
                .flatMap(realtimeBusInformation -> realtimeBusInformation.getArrivalBuses()
                        .stream()
                        .map(arrivalBus -> BusArrivalTime.from(realtimeBusInformation, arrivalBus, departureStop)));
    }

    private Stream<BusArrivalTime> fetchKakaoBusArrivalTimes(DepartureStop departureStop) {
        KakaoRealtimeBusInformation realtimeBusInformation =
                busArrivalTimeRequestManager.fetchKakaoRealTimeBusInformation(departureStop);
        return realtimeBusInformation.getArrivals()
                .stream()
                .flatMap(arrivalInformation -> arrivalInformation.getArrivalBuses()
                        .stream()
                        .map(arrivalBus -> BusArrivalTime.from(arrivalInformation, arrivalBus, departureStop)));
    }
}
