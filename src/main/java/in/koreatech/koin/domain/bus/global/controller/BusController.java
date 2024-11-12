package in.koreatech.koin.domain.bus.global.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.bus.city.dto.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.city.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.city.service.CityBusService;
import in.koreatech.koin.domain.bus.global.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.global.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.global.dto.BusTimetableResponse;
import in.koreatech.koin.domain.bus.global.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.global.model.BusTimetable;
import in.koreatech.koin.domain.bus.global.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.global.model.enums.BusType;
import in.koreatech.koin.domain.bus.global.service.BusRouteService;
import in.koreatech.koin.domain.bus.global.service.BusService;
import in.koreatech.koin.domain.bus.shuttle.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.shuttle.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus")
public class BusController implements BusApi {

    private final BusService busService;
    private final BusRouteService busRouteService;
    private final CityBusService cityBusService;

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
    ) {
        return ResponseEntity.ok().body(busService.getBusTimetable(busType, direction, region));
    }

    @GetMapping("/timetable/v2")
    public ResponseEntity<BusTimetableResponse> getBusTimetableV2(
        @RequestParam(value = "bus_type") BusType busType,
        @RequestParam(value = "direction") String direction,
        @RequestParam(value = "region") String region
    ) {
        return ResponseEntity.ok().body(busService.getBusTimetableWithUpdatedAt(busType, direction, region));
    }

    @GetMapping("/timetable/city")
    public ResponseEntity<CityBusTimetableResponse> getCityBusTimetable(
        @RequestParam(value = "bus_number") Long busNumber,
        @RequestParam(value = "direction") CityBusDirection direction
    ) {
        return ResponseEntity.ok().body(cityBusService.getCityBusTimetable(busNumber, direction));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<BusCourseResponse>> getBusCourses() {
        return ResponseEntity.ok().body(busService.getBusCourses());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SingleBusTimeResponse>> getSearchTimetable(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @RequestParam String time,
        @RequestParam BusStation depart,
        @RequestParam BusStation arrival
    ) {
        List<SingleBusTimeResponse> singleBusTimeResponses = busService.searchTimetable(date, LocalTime.parse(time),
            depart, arrival);
        return ResponseEntity.ok().body(singleBusTimeResponses);
    }

    @GetMapping("/route")
    public ResponseEntity<BusScheduleResponse> getBusRouteSchedule(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @RequestParam String time,
        @RequestParam BusRouteType busRouteType,
        @RequestParam BusStation depart,
        @RequestParam BusStation arrival
    ) {
        BusRouteCommand request = new BusRouteCommand(depart, arrival, busRouteType, date, LocalTime.parse(time));
        BusScheduleResponse busSchedule = busRouteService.getBusSchedule(request);
        return ResponseEntity.ok().body(busSchedule);
    }
}
