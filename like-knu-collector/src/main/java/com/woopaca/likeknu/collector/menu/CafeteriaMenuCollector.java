package com.woopaca.likeknu.collector.menu;

import com.woopaca.likeknu.collector.menu.constants.Cafeteria;
import com.woopaca.likeknu.collector.menu.domain.CafeteriaAttributes;
import com.woopaca.likeknu.collector.menu.dto.Meal;
import com.woopaca.likeknu.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CafeteriaMenuCollector implements MenuCollector {

    private final MenuProperties menuProperties;
    private final RestClient restClient;

    @Override
    public List<Meal> collectMenus() {
        return Arrays.stream(Cafeteria.values())
                .flatMap(cafeteria -> collectMenu(cafeteria).stream())
                .collect(Collectors.toList());
    }

    private List<Meal> collectMenu(Cafeteria cafeteria) {
        try {
            CompletableFuture<List<Meal>> thisWeekMealsFuture = CompletableFuture
                    .supplyAsync(() -> fetchMealsForWeek(cafeteria, false));
            CompletableFuture<List<Meal>> nextWeekMealsFuture = CompletableFuture
                    .supplyAsync(() -> fetchMealsForWeek(cafeteria, true));

            return thisWeekMealsFuture.thenCombine(
                    nextWeekMealsFuture, (thisWeekMeals, nextWeekMeals) -> Stream.concat(thisWeekMeals.stream(), nextWeekMeals.stream())
                            .toList()
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Meal> fetchMealsForWeek(Cafeteria cafeteria, boolean isNextWeek) {
        try {
            String url = isNextWeek ? generateNextWeekUrl(cafeteria) : generateUrl(cafeteria);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            CafeteriaAttributes cafeteriaAttributes = CafeteriaAttributes.from(document);
            return cafeteriaAttributes.stream()
                    .filter(cafeteriaAttribute -> cafeteriaAttribute.mealType() != null)
                    .map(cafeteriaAttribute -> Meal.of(cafeteria, cafeteriaAttribute))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateUrl(Cafeteria cafeteria) {
        return UriComponentsBuilder.fromUriString(menuProperties.getCafeteriaUrl())
                .buildAndExpand(cafeteria.getNumber())
                .toUriString();
    }

    private String generateNextWeekUrl(Cafeteria cafeteria) {
        LocalDate previousMonday = DateTimeUtils.getPreviousOrSameDate(DayOfWeek.MONDAY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return UriComponentsBuilder.fromUriString(menuProperties.getCafeteriaUrl())
                .queryParam("monday", formatter.format(previousMonday))
                .queryParam("week", "next")
                .buildAndExpand(cafeteria.getNumber())
                .toUriString();
    }
}
