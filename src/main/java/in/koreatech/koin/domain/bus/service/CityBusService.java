package in.koreatech.koin.domain.bus.service;

import static in.koreatech.koin.domain.bus.model.enums.BusStation.getDirection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.dto.city.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.facade.route.CityBusRouteManager;
import in.koreatech.koin.domain.bus.model.city.CityBusCache;
import in.koreatech.koin.domain.bus.model.city.CityBusRemainTime;
import in.koreatech.koin.domain.bus.model.city.CityBusRouteCache;
import in.koreatech.koin.domain.bus.model.city.CityBusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.bus.repository.CityBusRouteCacheRepository;
import in.koreatech.koin.domain.bus.repository.CityBusTimetableRepository;
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
        Set<Long> departAvailableBusNumbers = getAvailableCityBus(depart.getNodeId(direction));
        Set<Long> arrivalAvailableBusNumbers = getAvailableCityBus(arrival.getNodeId(direction));
        departAvailableBusNumbers.retainAll(arrivalAvailableBusNumbers);
        return getBusRemainTimeByNodeIds(depart.getNodeId(direction)).stream()
            .filter(remainTime -> departAvailableBusNumbers.contains(remainTime.getBusNumber()))
            .toList();
    }

    private Set<Long> getAvailableCityBus(List<String> nodeIds) {
        Set<Long> busNumbers = new HashSet<>();
        nodeIds.forEach(nodeId -> {
            Optional<CityBusRouteCache> routeCache = cityBusRouteCacheRepository.findById(nodeId);
            routeCache.ifPresent(cityBusRouteCache -> busNumbers.addAll(cityBusRouteCache.getBusNumbers()));
        });
        if (busNumbers.isEmpty()) {
            return new HashSet<>(AVAILABLE_CITY_BUS);
        }
        return busNumbers;
    }

    private List<CityBusRemainTime> getBusRemainTimeByNodeIds(List<String> nodeIds) {
        List<CityBusRemainTime> result = new ArrayList<>();
        nodeIds.forEach(nodeId -> {
            Optional<CityBusCache> cityBusCache = cityBusCacheRepository.findById(nodeId);
            cityBusCache.ifPresent(busCache -> {
                List<CityBusRemainTime> busRemainTimes = busCache.getBusInfos().stream()
                    .map(CityBusRemainTime::from)
                    .toList();
                result.addAll(busRemainTimes);
            });
        });
        return result;
    }

    public CityBusTimetableResponse getCityBusTimetable(Long busNumber, CityBusDirection direction) {
        CityBusTimetable timetable = cityBusTimetableRepository
            .getByBusInfoNumberAndBusInfoArrival(busNumber, direction.getName());
        return CityBusTimetableResponse.from(timetable);
    }

    public List<BusScheduleResponse.ScheduleInfo> getCityBusSchedule(
        Long busNumber,
        BusStation depart,
        CityBusDirection arrival,
        LocalDate date
    ) {
        CityBusTimetable timetable = cityBusTimetableRepository
            .getByBusInfoNumberAndBusInfoArrival(busNumber, arrival.getName());

        return CityBusRouteManager.getCityBusSchedule(timetable, busNumber, depart, arrival, date);
    }
}
