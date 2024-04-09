package in.koreatech.koin.domain.bus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.model.BusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.service.BusService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus")
public class BusController implements BusApi {

    private final BusService busService;

    @GetMapping
    public ResponseEntity<BusRemainTimeResponse> getBusRemainTime(
        @RequestParam(value = "bus_type") BusType busType,
        @RequestParam BusStation depart,
        @RequestParam BusStation arrival
    ) {
        BusRemainTimeResponse busRemainTime = busService.getBusRemainTime(busType, depart, arrival);
        return ResponseEntity.ok().body(busRemainTime);
    }

    @GetMapping("/timetable")
    public ResponseEntity<List<? extends BusTimetable>> getBusTimetable(
        @RequestParam(value = "bus_type") BusType busType,
        @RequestParam(value = "direction") String direction,
        @RequestParam(value = "region") String region
    ){
        return ResponseEntity.ok().body(busService.getBusTimetable(busType, direction, region));
    }
}
