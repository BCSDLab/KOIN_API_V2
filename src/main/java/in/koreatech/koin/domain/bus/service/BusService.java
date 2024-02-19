package in.koreatech.koin.domain.bus.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.model.BusCourse;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusStation;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;

    public BusRemainTimeResponse getBusRemainTime(String busType, String depart, String arrival) {
        List<BusCourse> all = busRepository.findAll();
        String direction = BusStation.getDirection(depart, arrival);

        List<BusCourse> foundBusCourses = busRepository.getByBusTypeAndDirection(busType, direction);
        BusRemainTime busRemainTime = BusRemainTime.from("10:13");
        // return BusRemainTimeResponse.from(foundBus);
        return null;
    }
}
