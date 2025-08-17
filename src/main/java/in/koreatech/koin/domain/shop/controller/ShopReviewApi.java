package in.koreatech.koin.domain.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import in.koreatech.koin.domain.shop.dto.review.request.CreateReviewRequest;
import in.koreatech.koin.domain.shop.dto.review.request.ModifyReviewRequest;
import in.koreatech.koin.domain.shop.dto.review.ReviewsSortCriteria;
import in.koreatech.koin.domain.shop.dto.review.response.ShopMyReviewsResponse;
import in.koreatech.koin.domain.shop.dto.review.response.ShopReviewReportCategoryResponse;
import in.koreatech.koin.domain.shop.dto.review.request.ShopReviewReportRequest;
import in.koreatech.koin.domain.shop.dto.review.response.ShopReviewResponse;
import in.koreatech.koin.domain.shop.dto.review.response.ShopReviewsResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "(Normal) ShopReview: 상점 리뷰", description = "상점 리뷰를 관리한다")
public interface ShopReviewApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 상점 리뷰 조회")
    @GetMapping("/shops/{shopId}/reviews")
    ResponseEntity<ShopReviewsResponse> getReviews(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "50", required = false) Integer limit,
        @RequestParam(name = "sorter", defaultValue = "LATEST") ReviewsSortCriteria sortBy,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "자신의 리뷰 조회")
    @GetMapping("/shops/{shopId}/reviews/me")
    ResponseEntity<ShopMyReviewsResponse> getMyReviews(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestParam(name = "sorter", defaultValue = "LATEST") ReviewsSortCriteria sortBy,
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 리뷰 조회")
    @GetMapping("/shops/{shopId}/reviews/{reviewId}")
    ResponseEntity<ShopReviewResponse> getReview(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @Parameter(in = PATH) @PathVariable Integer reviewId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "일일 리뷰 등록 횟수 초과", value = """
                {
                  "code": "",
                  "message": "한 상점에 하루에 한번만 리뷰를 남길 수 있습니다.",
                  "errorTraceId": "d4955051-bbc3-45fb-80d7-350eda9a81bd"
                }
                """)
            }), description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 상점 리뷰 작성")
    @PostMapping("/shops/{shopId}/reviews")
    ResponseEntity<Void> createReview(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestBody @Valid CreateReviewRequest createReviewRequest,
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 리뷰 수정")
    @PutMapping("/shops/{shopId}/reviews/{reviewId}")
    ResponseEntity<Void> modifyReview(
        @Parameter(in = PATH) @PathVariable Integer reviewId,
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestBody @Valid ModifyReviewRequest modifyReviewRequest,
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 리뷰 삭제")
    @DeleteMapping("/shops/{shopId}/reviews/{reviewId}")
    ResponseEntity<Void> deleteReview(
        @Parameter(in = PATH) @PathVariable Integer reviewId,
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "리뷰 신고 카테고리 조회")
    @GetMapping("/shops/reviews/reports/categories")
    ResponseEntity<ShopReviewReportCategoryResponse> getReportCategory(
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "리뷰 신고하기")
    @PostMapping("/shops/{shopId}/reviews/{reviewId}/reports")
    ResponseEntity<Void> reportReview(
        @Parameter(in = PATH) @PathVariable Integer reviewId,
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestBody @Valid ShopReviewReportRequest shopReviewReportRequest,
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    );
}
