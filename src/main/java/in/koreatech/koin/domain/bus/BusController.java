package in.koreatech.koin.domain.bus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.bus.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.dto.BusNoticeResponse;
import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.dto.BusTimetableResponse;
import in.koreatech.koin.domain.bus.service.city.dto.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.service.shuttle.dto.ShuttleBusRoutesResponse;
import in.koreatech.koin.domain.bus.service.shuttle.dto.ShuttleBusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.enums.BusRouteType;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.BusType;
import in.koreatech.koin.domain.bus.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.service.BusService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus")
public class BusController implements BusApi {

    private final BusService busService;

    // Deprecated: 강제 업데이트 이후 API 삭제 예정
    @GetMapping
    public ResponseEntity<BusRemainTimeResponse> getBusRemainTime(
        @RequestParam(value = "bus_type") BusType busType,
        @RequestParam BusStation depart,
        @RequestParam BusStation arrival
    ) {
        BusRemainTimeResponse busRemainTime = busService.getBusRemainTime(busType, depart, arrival);
        return ResponseEntity.ok().body(busRemainTime);
    }

    // 강제 업데이트 이후 BusTimetableV2 → ExpressBusTimetable 수정 예정
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

    // Deprecated: 강제 업데이트 이후 API 삭제 예정
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

    // Deprecated: 강제 업데이트 이후 API 삭제 예정
    @GetMapping("/courses")
    public ResponseEntity<List<BusCourseResponse>> getBusCourses() {
        return ResponseEntity.ok().body(busService.getBusCourses());
    }

    @GetMapping("/courses/shuttle")
    public ResponseEntity<ShuttleBusRoutesResponse> getShuttleBusRoutes() {
        return ResponseEntity.ok().body(busService.getShuttleBusRoutes());
    }

    @GetMapping("/timetable/shuttle/{id}")
    public ResponseEntity<ShuttleBusTimetableResponse> getShuttleBusTimetable(@PathVariable String id) {
        return ResponseEntity.ok().body(busService.getShuttleBusTimetable(id));
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
