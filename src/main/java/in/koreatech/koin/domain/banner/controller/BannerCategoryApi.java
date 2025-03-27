package in.koreatech.koin.domain.banner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.banner.dto.response.BannerCategoriesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Banner Category: 배너 카테고리", description = "배너 카테고리 정보를 관리한다")
@RequestMapping("/banner-categories")
public interface BannerCategoryApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "배너 카테고리 정보를 모두 조회한다.")
    @GetMapping
    ResponseEntity<BannerCategoriesResponse> getCategories();
}
