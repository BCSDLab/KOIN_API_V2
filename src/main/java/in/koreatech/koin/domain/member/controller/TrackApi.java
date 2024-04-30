package in.koreatech.koin.domain.member.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import in.koreatech.koin.domain.member.dto.TrackResponse;
import in.koreatech.koin.domain.member.dto.TrackSingleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(BCSDLab) Track: BCSDLab 트랙", description = "BCSDLab 트랙 정보를 관리한다")
public interface TrackApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "트랙 목록 조회")
    @GetMapping("/tracks")
    ResponseEntity<List<TrackResponse>> getTracks();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "트랙 단건 조회")
    @GetMapping("/tracks/{id}")
    ResponseEntity<TrackSingleResponse> getTrack(
        @Parameter(in = PATH) @PathVariable Integer id
    );
}
