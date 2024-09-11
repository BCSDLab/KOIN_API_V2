package in.koreatech.koin.domain.benefit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import in.koreatech.koin.domain.benefit.dto.BenefitCategoryResponse;
import in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;

@Tag(name = "(Normal) ShopBenefit: 상점 혜택", description = "상점 혜택 정보를 관리한다")
public interface BenefitApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 혜택 카테고리를 모두 조회한다.")
    @GetMapping("/benefit/categories")
    ResponseEntity<BenefitCategoryResponse> getBenefitCategories();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 혜택 카테고리에 속하는 상점을 모두 조회한다.")
    @GetMapping("/benefit/{id}/shops")
    ResponseEntity<BenefitShopsResponse> getBenefitShops(
        @PathParam("id") Integer benefitId
    );
}
