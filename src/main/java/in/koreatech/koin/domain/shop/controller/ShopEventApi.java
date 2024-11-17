package in.koreatech.koin.domain.shop.controller;

import in.koreatech.koin.domain.shop.dto.event.response.ShopEventsWithBannerUrlResponse;
import in.koreatech.koin.domain.shop.dto.event.response.ShopEventsWithThumbnailUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "(Normal) ShopEvent: 상점 이벤트", description = "상점 이벤트를 관리한다")
public interface ShopEventApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 상점의 모든 이벤트 조회")
    @GetMapping("/shops/{shopId}/events")
    ResponseEntity<ShopEventsWithThumbnailUrlResponse> getShopEvents(
        @PathVariable Integer shopId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "모든 상점의 모든 이벤트 조회")
    @GetMapping("/shops/events")
    ResponseEntity<ShopEventsWithBannerUrlResponse> getShopAllEvent();
}
