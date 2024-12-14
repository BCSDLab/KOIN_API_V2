package in.koreatech.koin.domain.bus.service;

import static in.koreatech.koin.domain.bus.model.enums.BusStation.getDirection;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.*;

import in.koreatech.koin.domain.bus.dto.*;
import in.koreatech.koin.domain.bus.repository.BusNoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.exception.BusIllegalStationException;
import in.koreatech.koin.domain.bus.exception.BusTypeNotFoundException;
import in.koreatech.koin.domain.bus.exception.BusTypeNotSupportException;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusTimetable;
import in.koreatech.koin.domain.bus.model.SchoolBusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.model.mongo.CityBusTimetable;
import in.koreatech.koin.domain.bus.model.mongo.Route;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.repository.CityBusTimetableRepository;
import in.koreatech.koin.domain.bus.util.city.CityBusClient;
import in.koreatech.koin.domain.bus.util.city.CityBusRouteClient;
import in.koreatech.koin.domain.bus.util.express.ExpressBusService;
import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusService {

    private final Clock clock;
    private final BusRepository busRepository;
    private final BusNoticeRepository busNoticeRepository;
    private final CityBusTimetableRepository cityBusTimetableRepository;
    private final CityBusClient cityBusClient;
    private final ExpressBusService expressBusService;
    private final CityBusRouteClient cityBusRouteClient;
    private final VersionService versionService;

    @Transactional
    public BusRemainTimeResponse getBusRemainTime(BusType busType, BusStation depart, BusStation arrival) {
        // 출발지 == 도착지면 예외
        validateBusCourse(depart, arrival);
        if (busType == BusType.CITY) {
            // 시내버스에서 상행, 하행 구분할때 사용하는 로직
            BusDirection direction = getDirection(depart, arrival);

            Set<Long> departAvailableBusNumbers = cityBusRouteClient.getAvailableCityBus(depart.getNodeId(direction));
            Set<Long> arrivalAvailableBusNumbers = cityBusRouteClient.getAvailableCityBus(arrival.getNodeId(direction));

            departAvailableBusNumbers.retainAll(arrivalAvailableBusNumbers);

            var remainTimes = cityBusClient.getBusRemainTime(depart.getNodeId(direction));

            remainTimes = remainTimes.stream()
                .filter(remainTime ->
                    departAvailableBusNumbers.contains(remainTime.getBusNumber())
                )
                .toList();

            return toResponse(busType, remainTimes);
        }

        if (busType == BusType.EXPRESS) {
            var remainTimes = expressBusService.getBusRemainTime(depart, arrival);
            return toResponse(busType, remainTimes);
        }

        if (busType == BusType.SHUTTLE || busType == BusType.COMMUTING) {
            List<BusCourse> busCourses = busRepository.findByBusType(busType.getName());
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

        throw new KoinIllegalArgumentException("Invalid bus", "type: " + busType);
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
                busTimeResponse = expressBusService.searchBusTime(
                    busType.getName(),
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

                LocalTime arrivalTime = busRepository.findByBusType(busType.getName()).stream()
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

                busTimeResponse = new SingleBusTimeResponse(busType.getName(), arrivalTime);
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

    public List<? extends BusTimetable> getBusTimetable(BusType busType, String direction, String region) {
        if (busType == BusType.CITY) {
            throw BusTypeNotSupportException.withDetail("busType: CITY");
        }

        if (busType == BusType.EXPRESS) {
            return expressBusService.getExpressBusTimetable(direction);
        }

        if (busType == BusType.SHUTTLE || busType == BusType.COMMUTING) {
            BusCourse busCourse = busRepository
                .getByBusTypeAndDirectionAndRegion(busType.getName(), direction, region);

            return busCourse.getRoutes().stream()
                .map(route -> new SchoolBusTimetable(
                    route.getRouteName(),
                    route.getArrivalInfos().stream()
                        .map(node -> new SchoolBusTimetable.ArrivalNode(
                            node.getNodeName(), node.getArrivalTime())
                        ).toList())).toList();
        }

        throw BusTypeNotFoundException.withDetail(busType.name());
    }

    public BusTimetableResponse getBusTimetableWithUpdatedAt(BusType busType, String direction, String region) {
        List<? extends BusTimetable> busTimetables = getBusTimetable(busType, direction, region);

        if (busType.equals(BusType.COMMUTING)) {
            busType = BusType.SHUTTLE;
        }

        VersionResponse version = versionService.getVersion(busType.getName() + "_bus_timetable");
        return new BusTimetableResponse(busTimetables, version.updatedAt());
    }

    public List<BusCourseResponse> getBusCourses() {
        return busRepository.findAll().stream()
            .map(BusCourseResponse::from)
            .toList();
    }

    public CityBusTimetableResponse getCityBusTimetable(Long busNumber, CityBusDirection direction) {
        CityBusTimetable timetable = cityBusTimetableRepository
            .getByBusInfoNumberAndBusInfoArrival(busNumber, direction.getName());

        return CityBusTimetableResponse.from(timetable);
    }

    public BusNoticeResponse getNotice() {
        Map<Object, Object> article = busNoticeRepository.getBusNotice();

        if (article == null || article.isEmpty()) {
            return null;
        }

        return BusNoticeResponse.of(
                (Integer) article.get("id"),
                (String) article.get("title")
        );
    }
}
