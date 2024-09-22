package in.koreatech.koin.domain.version.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.version.dto.VersionMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Version: 버전", description = "버전 정보를 관리한다")
@RequestMapping("/version")
public interface VersionApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 타입의 버전 조회")
    @GetMapping("/{type}")
    ResponseEntity<VersionMessageResponse> getVersionWithMessage(
        @Parameter(description = "android, ios, timetable, express_bus_timetable, shuttle_bus_timetable, city_bus_timetable")
        @PathVariable(value = "type") String type
    );
}
