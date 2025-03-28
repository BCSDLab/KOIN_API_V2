package in.koreatech.koin.domain.banner.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.banner.dto.response.BannersResponse;
import in.koreatech.koin.domain.banner.enums.PlatformType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Banner: 배너", description = "배너 정보를 관리한다")
@RequestMapping("/banners")
public interface BannerApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 카테고리, 플랫폼의 활성화된 배너들을 조회한다.")
    @GetMapping("/{categoryId}")
    ResponseEntity<BannersResponse> getBannersByCategoryAndPlatform(
        @Parameter(in = PATH) @PathVariable Integer categoryId,
        @Parameter(description = "플랫폼 타입(web, android, ios)") @RequestParam PlatformType platform
    );
}
