package in.koreatech.koin.domain.bus.service;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.exception.BusIllegalStation;
import in.koreatech.koin.domain.bus.model.BusCourse;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusStation;
import in.koreatech.koin.domain.bus.model.BusType;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusService {

    private final Clock clock;
    private final BusRepository busRepository;

    public BusRemainTimeResponse getBusRemainTime(String busTypeName, String departName, String arrivalName) {
        BusStation depart = BusStation.from(departName);
        BusStation arrival = BusStation.from(arrivalName);
        BusType busType = BusType.from(busTypeName);
        validateBusCourse(depart, arrival);

        List<BusCourse> busCourses = busRepository.findByBusType(busType.name().toLowerCase());
        List<BusRemainTime> remainTimes = busCourses.stream()
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

        return BusRemainTimeResponse.of(busType, remainTimes, clock);
    }

    private void validateBusCourse(BusStation depart, BusStation arrival) {
        if (depart.equals(arrival)) {
            throw BusIllegalStation.withDetail("depart: " + depart.name() + ", arrival: " + arrival.name());
        }
    }
}
