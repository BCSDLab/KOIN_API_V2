package in.koreatech.koin.domain.bus.controller;

import in.koreatech.koin.domain.bus.dto.*;
import in.koreatech.koin.domain.bus.model.BusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusRouteType;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.service.BusService;
import in.koreatech.koin.domain.bus.service.ShuttleBusService;
import in.koreatech.koin.domain.community.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus")
public class BusController implements BusApi {

    private final BusService busService;
    private final ShuttleBusService shuttleBusService;
    private final ArticleService articleService;

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
        return ResponseEntity.ok().body(shuttleBusService.getShuttleBusRoutes());
    }

    @GetMapping("/timetable/shuttle/{id}")
        public ResponseEntity<ShuttleBusTimetableResponse> getShuttleBusTimetable(@PathVariable String id) {
        return ResponseEntity.ok().body(shuttleBusService.getShuttleBusTimetable(id));
    }

    @GetMapping("/route")
    public ResponseEntity<BusScheduleResponse> getBusRouteSchedule(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @RequestParam String time,
        @RequestParam(value = "bus_type") BusRouteType busRouteType,
        @RequestParam BusStation depart,
        @RequestParam BusStation arrival
    ) {
        BusRouteCommand request = new BusRouteCommand(depart, arrival, busRouteType, date, LocalTime.parse(time));
        BusScheduleResponse busSchedule = busService.getBusSchedule(request);
        return ResponseEntity.ok().body(busSchedule);
    }

    @GetMapping("/notice")
    public ResponseEntity<BusNoticeResponse> getNotice() {
        BusNoticeResponse busNoticeResponse = busService.getNotice();
        return ResponseEntity.ok().body(busNoticeResponse);
    }
}
