package in.koreatech.koin.domain.bus.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.bus.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.dto.BusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.ShuttleBusRouteResponse;
import in.koreatech.koin.domain.bus.dto.ShuttleBusRoutesResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.model.BusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.repository.ShuttleBusRepository;
import in.koreatech.koin.domain.bus.service.BusService;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus")
public class BusController implements BusApi {

    private final BusService busService;
    private final ShuttleBusRepository shuttleBusRepository;
    private final VersionService versionService;

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
        return ResponseEntity.ok().body(busService.getCityBusTimetable(busNumber, direction));
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

    @GetMapping("/courses/shuttle")
    public ResponseEntity<ShuttleBusRoutesResponse> getShuttleBusRoutes() {
        return ResponseEntity.ok()
            .body(ShuttleBusRoutesResponse.of(shuttleBusRepository.findAll(),
                versionService.getVersionWithMessage("shuttle_bus_timetable")));
    }

    @GetMapping("/timetable/shuttle")
    public ResponseEntity<ShuttleBusRouteResponse> getShuttleBusTimetable(@RequestParam String id) {
        return ResponseEntity.ok().body(ShuttleBusRouteResponse.from(shuttleBusRepository.getById(id)));
    }
}
