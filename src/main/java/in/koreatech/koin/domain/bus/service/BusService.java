package in.koreatech.koin.domain.bus.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.model.Bus;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;

    public BusRemainTimeResponse getBusRemainTime(String busType, String depart, String arrival) {
        List<Bus> all = busRepository.findAll();
        // String direction = BusStation.getDirection(depart, arrival);
        // Bus foundBus = busRepository.getByBusTypeAndDirection(busType, direction);
        // return BusRemainTimeResponse.from(foundBus);
        return null;
    }
}
