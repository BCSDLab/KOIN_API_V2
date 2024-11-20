package in.koreatech.koin.domain.bus.service;

import static in.koreatech.koin.domain.bus.model.enums.BusStation.getDirection;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.dto.city.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.model.city.CityBusRemainTime;
import in.koreatech.koin.domain.bus.model.city.CityBusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.bus.repository.CityBusRouteCacheRepository;
import in.koreatech.koin.domain.bus.repository.CityBusTimetableRepository;
import in.koreatech.koin.domain.bus.service.route.CityBusRouteManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CityBusService {

    private static final Set<Long> AVAILABLE_CITY_BUS = Set.of(400L, 402L, 405L);

    private final CityBusRouteCacheRepository cityBusRouteCacheRepository;
    private final CityBusCacheRepository cityBusCacheRepository;
    private final CityBusTimetableRepository cityBusTimetableRepository;

    public List<CityBusRemainTime> getBusRemainTime(BusStation depart, BusStation arrival) {
        BusDirection direction = getDirection(depart, arrival);
        Set<Long> departAvailableBusNumbers = getAvailableCityBusNumbers(depart.getNodeId(direction));
        Set<Long> arrivalAvailableBusNumbers = getAvailableCityBusNumbers(arrival.getNodeId(direction));
        departAvailableBusNumbers.retainAll(arrivalAvailableBusNumbers); // 목적지 경유하지 않는 버스 제거
        return getCityBusRemainTimeList(depart.getNodeId(direction)).stream()
            .filter(remainTime -> departAvailableBusNumbers.contains(remainTime.getBusNumber()))
            .toList();
    }

    private Set<Long> getAvailableCityBusNumbers(List<String> nodeIds) {
        Set<Long> busNumbers = nodeIds.stream()
            .map(cityBusRouteCacheRepository::findById)
            .filter(Optional::isPresent).map(Optional::get)
            .flatMap(routeCache -> routeCache.getBusNumbers().stream())
            .collect(Collectors.toSet());
        return busNumbers.isEmpty() ? new HashSet<>(AVAILABLE_CITY_BUS) : busNumbers;
    }

    private List<CityBusRemainTime> getCityBusRemainTimeList(List<String> nodeIds) {
        return nodeIds.stream()
            .map(cityBusCacheRepository::findById)
            .filter(Optional::isPresent).map(Optional::get)
            .flatMap(busCache -> busCache.getBusInfos().stream())
            .map(CityBusRemainTime::from)
            .toList();
    }

    public CityBusTimetableResponse getCityBusTimetable(Long busNumber, CityBusDirection direction) {
        CityBusTimetable timetable = cityBusTimetableRepository
            .getByBusInfoNumberAndBusInfoArrival(busNumber, direction.getName());
        return CityBusTimetableResponse.from(timetable);
    }

    public List<BusScheduleResponse.ScheduleInfo> getCityBusSchedule(Long busNumber, BusStation depart,
        CityBusDirection arrival, LocalDate date) {
        CityBusTimetable timetable = cityBusTimetableRepository
            .getByBusInfoNumberAndBusInfoArrival(busNumber, arrival.getName());
        return CityBusRouteManager.getCityBusSchedule(timetable, busNumber, depart, arrival, date);
    }
}
