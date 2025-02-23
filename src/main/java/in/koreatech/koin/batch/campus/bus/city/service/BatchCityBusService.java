package in.koreatech.koin.batch.campus.bus.city.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.batch.campus.bus.city.dto.CityBusRouteApiResponse;
import in.koreatech.koin.batch.campus.bus.city.dto.CityBusRouteInfo;
import in.koreatech.koin.batch.campus.bus.city.dto.CityBusTimetableApiResponse;
import in.koreatech.koin.batch.campus.bus.city.dto.CityBusTimetableApiResponse.InnerRoute;
import in.koreatech.koin.batch.campus.bus.city.model.CityBusTimetable;
import in.koreatech.koin.batch.campus.bus.city.model.TimetableDocument;
import in.koreatech.koin.batch.campus.bus.city.repository.BatchCityBusTimetableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BatchCityBusService {

    private final RestTemplate restTemplate;
    private final BatchCityBusTimetableRepository batchCityBusTimetableRepository;

    public void update() {
        List<TimetableDocument> timetables = crawling();

        batchCityBusTimetableRepository.saveAll(timetables);

        // TODO : Save Timetables using Bulk Write
        // TODO : Remove Duplicate Entities
        // TODO : Change System.out.println to Logger
    }

    private List<TimetableDocument> crawling() {
        List<Long> availableBuses = getAvailableBus();
        System.out.println(availableBuses);

        List<Long> routeIds = availableBuses.stream()
            .flatMap(busNumber -> getRouteIds(busNumber).stream())
            .toList();
        System.out.println(routeIds);

        List<CityBusRouteInfo> routeInfos = routeIds.stream()
            .flatMap(routeId -> getRouteInfo(routeId).stream())
            .toList();
        System.out.println(routeInfos);

        List<TimetableDocument> timetables = routeInfos.stream().map(this::getTimetable).toList();
        System.out.println(timetables);

        return timetables;
    }

    private List<Long> getAvailableBus() {
        return List.of(400L, 402L, 405L);
    }

    private List<Long> getRouteIds(Long busNumber) {
        ResponseEntity<CityBusTimetableApiResponse> response = restTemplate.getForEntity(
            "https://its.cheonan.go.kr/bis/getBusTimeTable.do?thisPage=1&searchKeyword={busNumber}",
            CityBusTimetableApiResponse.class,
            busNumber
        );

        if (!response.hasBody()) {
            return Collections.emptyList();
        }

        return Objects.requireNonNull(response.getBody())
            .resultList()
            .stream()
            .map(InnerRoute::routeId)
            .toList();
    }

    private List<CityBusRouteInfo> getRouteInfo(Long routeId) {
        ResponseEntity<CityBusRouteApiResponse> response = restTemplate.getForEntity(
            "https://its.cheonan.go.kr/bis/getRouteList.do?searchKeyword={routeId}",
            CityBusRouteApiResponse.class,
            routeId
        );

        if (!response.hasBody()) {
            return Collections.emptyList();
        }

        return Objects.requireNonNull(response.getBody()).resultList();
    }

    private TimetableDocument getTimetable(CityBusRouteInfo cityBusRouteInfo) {
        String url = "https://its.cheonan.go.kr/bis/showBusTimeTable.do";

        Document document;
        try {
            document = Jsoup.connect(url)
                .data("routeName", cityBusRouteInfo.routeName())
                .data("routeDirection", cityBusRouteInfo.routeDirection())
                .data("relayAreacode", cityBusRouteInfo.relayAreaCode())
                .data("routeExplain", cityBusRouteInfo.routeExplain())
                .data("stName", cityBusRouteInfo.stName())
                .data("edName", cityBusRouteInfo.edName())
                .ignoreContentType(true)
                .timeout(5000)
                .get();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> dayOfWeeks = List.of("평일", "주말", "공휴일", "임시");
        List<CityBusTimetable> timetables = new ArrayList<>();
        for (int i = 0; i < dayOfWeeks.size(); i++) {
            String selector =
                "body > div.timeTalbeWrap > div > div.timeTable-wrap > div:nth-child(" + (i + 1) + ") > dl > dd";
            Elements departInfoElem = document.select(selector);

            List<String> departInfo = departInfoElem.stream()
                .map(element -> element.text().substring(0, 5))
                .toList();

            String dayOfWeek = dayOfWeeks.get(i);
            timetables.add(
                CityBusTimetable.builder().dayOfWeek(dayOfWeek).departInfo(departInfo).build()
            );
        }

        return TimetableDocument.builder()
            .busTimetables(timetables)
            .routeInfo(cityBusRouteInfo)
            .updatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
            .build();
    }
}
