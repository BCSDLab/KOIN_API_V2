package in.koreatech.koin.domain.bus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Bus: 버스", description = "버스 정보를 조회한다.")
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
        @RequestParam(value = "bus_type") String busType,
        @RequestParam String depart,
        @RequestParam String arrival
    );
}
