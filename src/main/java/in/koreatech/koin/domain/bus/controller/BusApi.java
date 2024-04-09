package in.koreatech.koin.domain.bus.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.bus.dto.BusCourseResponse;
import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
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
    @GetMapping("/bus")
    ResponseEntity<BusRemainTimeResponse> getBusRemainTime(
        @Parameter(description = "버스 종류(city, express, shuttle, commuting)") @RequestParam(value = "bus_type") BusType busType,
        @Parameter(description = "koreatech, station, terminal") @RequestParam BusStation depart,
        @Parameter(description = "koreatech, station, terminal") @RequestParam BusStation arrival
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
}
