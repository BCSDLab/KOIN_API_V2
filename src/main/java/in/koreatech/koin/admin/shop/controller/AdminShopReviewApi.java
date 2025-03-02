package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.shop.dto.review.AdminModifyShopReviewReportStatusRequest;
import in.koreatech.koin.admin.shop.dto.review.AdminShopsReviewsResponse;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) ShopReview: 상점 리뷰", description = "관리자 권한으로 상점 리뷰를 관리한다")
public interface AdminShopReviewApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "리뷰 조회")
    @GetMapping("/admin/shops/reviews")
    ResponseEntity<AdminShopsReviewsResponse> getReviews(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_reported", required = false) Boolean isReported,
        @RequestParam(name = "is_have_unhandled_report", required = false) Boolean isHaveUnhandledReport,
        @RequestParam(name = "shop_id", required = false) Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "리뷰 신고 상태 변경")
    @PutMapping("/admin/shops/reviews/{id}")
    ResponseEntity<Void> modifyReviewReportStatus(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminModifyShopReviewReportStatusRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/admin/shops/reviews/{id}")
    ResponseEntity<Void> deleteReview(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
