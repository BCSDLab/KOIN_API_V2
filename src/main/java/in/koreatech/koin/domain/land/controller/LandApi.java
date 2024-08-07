package in.koreatech.koin.domain.land.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import in.koreatech.koin.domain.land.dto.LandResponse;
import in.koreatech.koin.domain.land.dto.LandsGroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Land: 복덕방", description = "복덕방 정보를 관리한다")
public interface LandApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "복덕방 목록 조회")
    @GetMapping("/lands")
    ResponseEntity<LandsGroupResponse> getLands();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "복덕방 단건 조회")
    @GetMapping("/lands/{id}")
    ResponseEntity<LandResponse> getLand(
        @Parameter(in = PATH) @PathVariable Integer id
    );
}
