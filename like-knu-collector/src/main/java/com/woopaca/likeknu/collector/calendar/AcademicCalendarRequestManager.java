package com.woopaca.likeknu.collector.calendar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
public class AcademicCalendarRequestManager {

    private final WebProperties webProperties;
    private final WebClient webClient;

    public AcademicCalendarRequestManager(WebProperties webProperties, WebClient webClient) {
        this.webProperties = webProperties;
        this.webClient = webClient;
    }

    public String fetchAcademicCalendarPage(int year, int month) {
        URI uri = UriComponentsBuilder.fromUriString(webProperties.getAcademicCalendar())
                .queryParam("year", year)
                .queryParam("month", month)
                .build()
                .toUri();

        String responseBody = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (responseBody == null) {
            log.error("[{}] Response body is null!", getClass().getName());
            throw new IllegalArgumentException(String.format("[%s] Response body is null!", getClass().getName()));
        }

        return responseBody;
    }
}
