package in.koreatech.koin.domain.bus.facade;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.service.CityBusService;
import in.koreatech.koin.domain.bus.service.ExpressBusService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusScheduleService {

    private final CityBusService cityBusService;
    private final ExpressBusService expressBusService;

    public void storeCityBusRemainTime() {
        cityBusService.storeRemainTime();
    }

    public void storeCityBusRoute() {
        cityBusService.storeCityBusRoute();
    }

    public void storeExpressBusRemainTime() {
        expressBusService.storeRemainTimeByRatio();
    }
}
