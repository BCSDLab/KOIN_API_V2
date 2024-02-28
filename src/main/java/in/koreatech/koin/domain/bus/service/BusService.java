package in.koreatech.koin.domain.bus.service;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.model.BusCourse;
import in.koreatech.koin.domain.bus.model.BusDirection;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusStation;
import in.koreatech.koin.domain.bus.model.BusType;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.util.BusOpenApiRequestor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusService {

    private final Clock clock;
    private final BusRepository busRepository;
    private final BusOpenApiRequestor busOpenApiRequestor;

    public BusRemainTimeResponse getBusRemainTime(String busTypeStr, String departStr, String arrivalStr) {

        BusStation departStation = BusStation.from(departStr);
        BusStation arrivalStation = BusStation.from(arrivalStr);
        BusDirection direction = BusStation.getDirection(departStation, arrivalStation);
        BusType busType = BusType.from(busTypeStr);


        // =====================================


        // String result1 = busOpenApiRequestor.getCityBusArrivalInfo(departStation.getNodeId(direction));


        // =====================================

        List<BusRemainTime> remainTimes = busRepository.getByBusType(busType).stream()
            .map(BusCourse::getRoutes)
            .flatMap(routes ->
                routes.stream()
                    .filter(route -> route.isRunning(clock))
                    .filter(route -> route.isCorrectRoute(departStation, arrivalStation, clock))
                    .map(route -> route.getRemainTime(departStation))
            )
            .distinct()
            .sorted()
            .toList();

        return BusRemainTimeResponse.of(busType, remainTimes, clock);
    }
}
