package in.koreatech.koin.domain.bus.service;

import java.time.Clock;
import java.util.ArrayList;
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
import in.koreatech.koin.domain.bus.model.enums.IntercityBusStationNode;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.util.CityBusOpenApiClient;
import in.koreatech.koin.domain.bus.util.IntercityBusOpenApiClient;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusService {

    private final Clock clock;
    private final BusRepository busRepository;
    private final CityBusOpenApiClient cityBusOpenApiClient;
    private final IntercityBusOpenApiClient intercityBusOpenApiClient;

    @Transactional
    public BusRemainTimeResponse getBusRemainTime(String busTypeName, String departName, String arrivalName) {
        BusType busType = BusType.from(busTypeName);
        BusStation depart = BusStation.from(departName);
        BusStation arrival = BusStation.from(arrivalName);
        BusDirection direction = BusStation.getDirection(depart, arrival);
        validateBusCourse(depart, arrival);

        List<? extends BusRemainTime> remainTimes = new ArrayList<>();
        if (busType == BusType.CITY) {
            remainTimes = cityBusOpenApiClient.getBusRemainTime(depart.getNodeId(direction));
        } else if (busType == BusType.EXPRESS) {
            remainTimes = intercityBusOpenApiClient.getBusRemainTime(IntercityBusStationNode.getId(departName),
                IntercityBusStationNode.getId(arrivalName));
        } else if (busType == BusType.SHUTTLE || busType == BusType.COMMUTING) {
            List<BusCourse> busCourses = busRepository.findByBusType(busType.name().toLowerCase());
            remainTimes = busCourses.stream()
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
        }

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
}
