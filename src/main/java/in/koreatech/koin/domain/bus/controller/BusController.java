package in.koreatech.koin.domain.bus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.service.BusService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BusController implements BusApi {

    private final BusService busService;

    @Override
    public ResponseEntity<BusRemainTimeResponse> getBusRemainTime(
        @RequestParam(value = "bus_type") String busType,
        @RequestParam String depart,
        @RequestParam String arrival
    ) {
        BusRemainTimeResponse busRemainTime = busService.getBusRemainTime(busType, depart, arrival);
        return ResponseEntity.ok().body(busRemainTime);
    }
}
