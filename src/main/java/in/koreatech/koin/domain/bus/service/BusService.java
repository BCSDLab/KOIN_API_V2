package in.koreatech.koin.domain.bus.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.exception.BusIllegalStationException;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.model.mongo.Route;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.util.CityBusOpenApiClient;
import in.koreatech.koin.domain.bus.util.ExpressBusOpenApiClient;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusService {

    private final Clock clock;
    private final BusRepository busRepository;
    private final CityBusOpenApiClient cityBusOpenApiClient;
    private final ExpressBusOpenApiClient expressBusOpenApiClient;

    @Transactional
    public BusRemainTimeResponse getBusRemainTime(BusType busType, BusStation depart, BusStation arrival) {
        // 출발지 == 도착지면 예외
        validateBusCourse(depart, arrival);
        if (busType == BusType.CITY) {
            // 시내버스에서 상행, 하행 구분할때 사용하는 로직
            BusDirection direction = BusStation.getDirection(depart, arrival);
            var remainTimes = cityBusOpenApiClient.getBusRemainTime(depart.getNodeId(direction));
            return toResponse(busType, remainTimes);
        }

        if (busType == BusType.EXPRESS) {
            var remainTimes = expressBusOpenApiClient.getBusRemainTime(depart, arrival);
            return BusRemainTimeResponse.of(busType, remainTimes, clock);
        }

        if (busType == BusType.SHUTTLE || busType == BusType.COMMUTING) {
            List<BusCourse> busCourses = busRepository.findByBusType(busType.name().toLowerCase());
            var remainTimes = busCourses.stream()
                .map(BusCourse::getRoutes)
                .flatMap(routes ->
                    routes.stream()
                        .filter(route -> route.isRunning(clock))
                        .filter(route -> route.isCorrectRoute(depart, arrival, clock))
                        .map(route -> route.getRemainTime(depart))
                )
                .distinct()
                .sorted()
                .toList();
            return toResponse(busType, remainTimes);
        }

        throw new IllegalArgumentException("Invalid bus type: " + busType);
    }

    public List<SingleBusTimeResponse> searchTimetable(
        LocalDate date, LocalTime time,
        BusStation depart, BusStation arrival
    ) {
        validateBusCourse(depart, arrival);
        List<SingleBusTimeResponse> result = new ArrayList<>();

        LocalDateTime targetTime = LocalDateTime.of(date, time);
        for (BusType busType : BusType.values()) {
            SingleBusTimeResponse busTimeResponse = null;

            if (busType == BusType.EXPRESS) {
                busTimeResponse = expressBusOpenApiClient.searchBusTime(
                    busType.name().toLowerCase(),
                    depart,
                    arrival,
                    targetTime
                );
            }

            if (busType == BusType.SHUTTLE || busType == BusType.COMMUTING) {
                ZonedDateTime zonedAt = targetTime.atZone(clock.getZone());
                Clock clockAt = Clock.fixed(zonedAt.toInstant(), zonedAt.getZone());

                String todayName = targetTime.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.US)
                    .toUpperCase();

                LocalTime arrivalTime = busRepository.findByBusType(busType.name().toLowerCase()).stream()
                    .filter(busCourse -> busCourse.getRegion().equals("천안"))
                    .map(BusCourse::getRoutes)
                    .flatMap(routes ->
                        routes.stream()
                            .filter(route -> route.getRunningDays().contains(todayName))
                            .filter(route -> route.isRunning(clockAt))
                            .filter(route -> route.isCorrectRoute(depart, arrival, clockAt))
                            .flatMap(route ->
                                route.getArrivalInfos().stream()
                                    .filter(arrivalNode -> depart.getDisplayNames().contains(arrivalNode.getNodeName()))
                            )
                    )
                    .min(Comparator.comparing(o -> LocalTime.parse(o.getArrivalTime())))
                    .map(Route.ArrivalNode::getArrivalTime)
                    .map(LocalTime::parse)
                    .orElse(null);

                busTimeResponse = new SingleBusTimeResponse(busType.name().toLowerCase(), arrivalTime);
            }

            if (busTimeResponse == null) {
                continue;
            }
            result.add(busTimeResponse);
        }

        return result;
    }

    private BusRemainTimeResponse toResponse(BusType busType, List<? extends BusRemainTime> remainTimes) {
        return BusRemainTimeResponse.of(
            busType,
            remainTimes.stream()
                .filter(bus -> bus.getRemainSeconds(clock) != null)
                .sorted(Comparator.naturalOrder())
                .toList(),
            clock
        );
    }

    private void validateBusCourse(BusStation depart, BusStation arrival) {
        if (depart.equals(arrival)) {
            throw BusIllegalStationException.withDetail("depart: " + depart.name() + ", arrival: " + arrival.name());
        }
    }

    public List<BusCourseResponse> getBusCourses() {
        return busRepository.findAll().stream()
            .map(BusCourseResponse::from)
            .toList();
    }

}
