package in.koreatech.koin.domain.activity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.activity.dto.ActivitiesResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Activity", description = "BCSDLab 활동")
public interface ActivityApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 활동 목록을 조회함"),
            @ApiResponse(responseCode = "404", description = "해당하는 활동이 없음", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "BCSD Lab 활동 조회")
    @GetMapping("/activities")
    ResponseEntity<ActivitiesResponseDTO> getActivities(
        @RequestParam(required = false) String year
    );
}
