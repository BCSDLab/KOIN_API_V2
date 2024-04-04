package in.koreatech.koin.domain.bus.service;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.exception.BusIllegalStationException;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
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
            var remainTimes = expressBusOpenApiClient.getBusRemainTime(depart.name(), arrival.name());
            return new BusRemainTimeResponse(busType, remainTimes);
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
            throw BusIllegalStationException.withDetail(
                "depart: " + depart.name() + ", arrivalTime: " + arrival.name());
        }
    }
}
