package in.koreatech.koin.domain.dining.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.dining.dto.SoldOutRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Coop Dining : 영양사 식단", description = "영양사 식단 페이지")
public interface CoopDiningApi {
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "특정 코너 품절 요청")
    @GetMapping("/coop/dining/soldout")
    ResponseEntity<List<Void>> changeSoldOut(
        @Parameter(description = "사용자 ID") @RequestParam(required = false) Long userId,
        @RequestBody SoldOutRequest soldOutRequest
    );
}
