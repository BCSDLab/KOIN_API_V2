package in.koreatech.koin.domain.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import in.koreatech.koin.domain.shop.dto.search.RelatedKeyword;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.shop.dto.review.CreateReviewRequest;
import in.koreatech.koin.domain.shop.dto.menu.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.review.ModifyReviewRequest;
import in.koreatech.koin.domain.shop.dto.review.ReviewsSortCriteria;
import in.koreatech.koin.domain.shop.dto.shop.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopEventsResponse;
import in.koreatech.koin.domain.shop.dto.menu.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.review.ShopMyReviewsResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopResponse;
import in.koreatech.koin.domain.shop.dto.review.ShopReviewReportCategoryResponse;
import in.koreatech.koin.domain.shop.dto.review.ShopReviewReportRequest;
import in.koreatech.koin.domain.shop.dto.review.ShopReviewResponse;
import in.koreatech.koin.domain.shop.dto.review.ShopReviewsResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.shop.ShopsResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopsResponseV2;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteria;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Shop: 상점", description = "상점 정보를 관리한다")
public interface ShopApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "메뉴 단건 조회")
    @GetMapping("/shops/{shopId}/menus/{menuId}")
    ResponseEntity<MenuDetailResponse> findMenu(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @Parameter(in = PATH) @PathVariable Integer menuId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 상점의 모든 메뉴 조회")
    @GetMapping("/shops/{id}/menus")
    ResponseEntity<ShopMenuResponse> findMenus(
        @Parameter(in = PATH) @PathVariable Integer id
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "메뉴 카테고리 목록 조회")
    @GetMapping("/shops/{shopId}/menus/categories")
    ResponseEntity<MenuCategoriesResponse> getMenuCategories(
        @Parameter(in = PATH) @PathVariable Integer shopId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 상점 조회")
    @GetMapping("/shops/{id}")
    ResponseEntity<ShopResponse> getShopById(
        @Parameter(in = PATH) @PathVariable Integer id
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "모든 상점 조회")
    @GetMapping("/shops")
    ResponseEntity<ShopsResponse> getShops();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "모든 상점 카테고리 조회")
    @GetMapping("/shops/categories")
    ResponseEntity<ShopCategoriesResponse> getShopsCategories();

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
    ResponseEntity<ShopEventsResponse> getShopEvents(
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
    ResponseEntity<ShopEventsResponse> getShopAllEvent();

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
        @Auth(permit = {STUDENT}) Integer studentId
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
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
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
        @Auth(permit = {STUDENT}) Integer studentId
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
        @Auth(permit = {STUDENT}) Integer studentId
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
        @Auth(permit = {STUDENT}) Integer studentId
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
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "정렬, 필터가 있는 모든 상점 조회")
    @GetMapping("/v2/shops")
    ResponseEntity<ShopsResponseV2> getShopsV2(
        @RequestParam(name = "sorter", defaultValue = "NONE") ShopsSortCriteria sortBy,
        @RequestParam(name = "filter") List<ShopsFilterCriteria> shopsFilterCriterias,
        @RequestParam(name = "query") String query
    );

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            }
    )
    @Operation(summary = "주변상점 검색어에 따른 연관검색어 조회")
    @GetMapping("/search/related/{query}")
    ResponseEntity<RelatedKeyword> getRelatedKeyword(
            @PathVariable(name = "query") String query
    );
}
