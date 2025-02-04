package in.koreatech.koin.domain.bus.service;

import static in.koreatech.koin.domain.bus.model.enums.BusStation.getDirection;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.model.city.CityBusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.model.mongo.CityBusTimetable;
import in.koreatech.koin.domain.bus.repository.CityBusTimetableRepository;
import in.koreatech.koin.domain.bus.util.city.CityBusClient;
import in.koreatech.koin.domain.bus.util.city.CityBusRouteClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CityBusService {

    private final CityBusClient cityBusClient;
    private final CityBusRouteClient cityBusRouteClient;
    private final CityBusTimetableRepository cityBusTimetableRepository;

    // 시내버스에서 상행, 하행 구분할때 사용하는 로직
    public List<CityBusRemainTime> getBusRemainTime(BusStation depart, BusStation arrival) {
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
        return remainTimes;
    }

    public CityBusTimetableResponse getCityBusTimetable(Long busNumber, CityBusDirection direction) {
        CityBusTimetable timetable = cityBusTimetableRepository
            .getByBusInfoNumberAndBusInfoArrival(busNumber, direction.getName());
        return CityBusTimetableResponse.of(busNumber, direction, timetable);
    }
}
