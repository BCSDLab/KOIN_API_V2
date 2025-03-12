package in.koreatech.koin.domain.bus.service.city;

import static in.koreatech.koin.domain.bus.enums.BusStation.getDirection;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.service.city.dto.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.service.city.model.CityBusRemainTime;
import in.koreatech.koin.domain.bus.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.service.city.model.CityBusTimetable;
import in.koreatech.koin.domain.bus.service.city.repository.CityBusTimetableRepository;
import in.koreatech.koin.domain.bus.service.city.client.CityBusClient;
import in.koreatech.koin.domain.bus.service.city.client.CityBusRouteClient;
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
