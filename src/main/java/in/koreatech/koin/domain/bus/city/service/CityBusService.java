package in.koreatech.koin.domain.bus.city.service;

import static in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation.getDirection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.city.dto.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.city.model.CityBusCache;
import in.koreatech.koin.domain.bus.city.model.CityBusRemainTime;
import in.koreatech.koin.domain.bus.city.model.CityBusRouteCache;
import in.koreatech.koin.domain.bus.city.model.CityBusTimetable;
import in.koreatech.koin.domain.bus.city.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.city.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.city.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.bus.city.repository.CityBusRouteCacheRepository;
import in.koreatech.koin.domain.bus.city.repository.CityBusTimetableRepository;
import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;
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
}
