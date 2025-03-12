package in.koreatech.koin.domain.bus;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.bus.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.dto.BusNoticeResponse;
import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Bus: 버스", description = "버스 정보를 조회한다.")
@RequestMapping("/bus")
public interface BusApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "이번 / 다음 버스 남은 시간 조회")
    @GetMapping
    ResponseEntity<BusRemainTimeResponse> getBusRemainTime(
        @Parameter(description = "버스 종류(city, express, shuttle, commuting)") @RequestParam(value = "bus_type") BusType busType,
        @Parameter(description = "koreatech, station, terminal") @RequestParam BusStation depart,
        @Parameter(description = "koreatech, station, terminal") @RequestParam BusStation arrival
    );

    @Operation(summary = "버스 시간표 조회")
    @GetMapping("/timetable/v2")
    ResponseEntity<BusTimetableResponse> getBusTimetableV2(
        @RequestParam(value = "bus_type") BusType busType,
        @RequestParam(value = "direction") String direction,
        @RequestParam(value = "region") String region
    );

    @Operation(summary = "시내버스 시간표 조회")
    @GetMapping("/timetable/city")
    ResponseEntity<CityBusTimetableResponse> getCityBusTimetable(
        @RequestParam(value = "bus_number") Long bus_number,
        @RequestParam(value = "direction") CityBusDirection direction
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "날짜 & 시간 기준 버스 검색")
    @GetMapping("/search")
    ResponseEntity<List<SingleBusTimeResponse>> getSearchTimetable(
        @Parameter(description = "yyyy-MM-dd") @RequestParam LocalDate date,
        @Parameter(description = "HH:mm") @RequestParam String time,
        @Parameter(description = "koreatech, station, terminal") @RequestParam BusStation depart,
        @Parameter(description = "koreatech, station, terminal") @RequestParam BusStation arrival
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "버스 노선 조회")
    @GetMapping("/courses")
    ResponseEntity<List<BusCourseResponse>> getBusCourses();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
        }
    )
    @Operation(summary = "학교버스 노선 조회")
    @GetMapping("/courses/shuttle")
    ResponseEntity<ShuttleBusRoutesResponse> getShuttleBusRoutes();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "학교버스 특정 노선 시간표 조회", description = "id: 노선 id 값 (get /bus/courses/shuttle response 참조)")
    @GetMapping("/timetable/shuttle/{id}")
    ResponseEntity<ShuttleBusTimetableResponse> getShuttleBusTimetable(@PathVariable String id);

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "버스 교통편 조회",
        description = """
            ### 버스 교통편 조회
            - **시간** : 13:00 인 경우 13시 이후 출발하는 버스의 시간표를 조회합니다. 00:00 인 경우 해당 날짜의 모든 스케줄을 조회합니다.
            - **날짜** : 요일을 기준으로 스케줄을 출력합니다. 공휴일 처리는 구현되어 있지 않습니다.
            - **출발지 & 도착지** : 출발지와 도착지가 일치하는 경우 빈 리스트를 반환합니다.
            """
    )
    @GetMapping("/route")
    ResponseEntity<BusScheduleResponse> getBusRouteSchedule(
        @Parameter(description = "yyyy-MM-dd") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @Parameter(description = "HH:mm") @RequestParam String time,
        @RequestParam BusRouteType busRouteType,
        @RequestParam BusStation depart,
        @RequestParam BusStation arrival
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "버스 긴급 공지 조회")
    @GetMapping("/notice")
    ResponseEntity<BusNoticeResponse> getNotice();
}
