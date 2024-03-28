package in.koreatech.koin.domain.bus.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.exception.BusIllegalStationException;
import in.koreatech.koin.domain.bus.model.enums.ApiType;
import in.koreatech.koin.domain.bus.model.Bus;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.util.BusOpenApiRequester;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusService {

    private final Clock clock;
    private final BusRepository busRepository;
    private final Map<String, BusOpenApiRequester<? extends Bus>> busOpenApiRequesters;

    @Transactional
    public BusRemainTimeResponse getBusRemainTime(String busTypeName, String departName, String arrivalName) {
        BusType busType = BusType.from(busTypeName);
        BusStation depart = BusStation.from(departName);
        BusStation arrival = BusStation.from(arrivalName);
        BusDirection direction = BusStation.getDirection(depart, arrival);
        validateBusCourse(depart, arrival);

        List<? extends Bus> remainTimes = new ArrayList<>();
        if (busType == BusType.CITY || busType == BusType.EXPRESS) {
            remainTimes = busOpenApiRequesters.get(ApiType.from(busType).getValue())
                .getBusRemainTime(depart.getNodeId(direction));
        }
        else if (busType == BusType.SHUTTLE || busType == BusType.COMMUTING) {
            List<BusCourse> busCourses = busRepository.findByBusType(busType.name().toLowerCase());
            remainTimes = busCourses.stream()
                .map(BusCourse::getRoutes)
                .flatMap(routes ->
                    routes.stream()
                        .filter(route -> route.isRunning(clock))
                        .filter(route -> route.isCorrectRoute(depart, arrival, clock))
                        .map(route -> route.getRemainTime(depart))
                        .map(Bus::from)
                )
                .distinct()
                .sorted()
                .toList();
        }

        return BusRemainTimeResponse.of(
            busType,
            remainTimes.stream()
                .filter(bus -> bus.getRemainTime().getRemainSeconds(clock) != null)
                .sorted(Comparator.comparing(Bus::getRemainTime))
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
