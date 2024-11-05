package in.koreatech.koin.domain.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import in.koreatech.koin.domain.shop.dto.search.RelatedKeyword;
import in.koreatech.koin.domain.shop.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import in.koreatech.koin.domain.shop.service.ShopReviewService;
import in.koreatech.koin.domain.shop.service.ShopService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShopController implements ShopApi {

    private final ShopService shopService;
    private final ShopReviewService shopReviewService;
    private final SearchService searchService;

    @GetMapping("/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<MenuDetailResponse> findMenu(
        @PathVariable Integer shopId,
        @PathVariable Integer menuId
    ) {
        MenuDetailResponse shopMenu = shopService.findMenu(menuId);
        return ResponseEntity.ok(shopMenu);
    }

    @GetMapping("/shops/{id}/menus")
    public ResponseEntity<ShopMenuResponse> findMenus(
        @PathVariable Integer id
    ) {
        ShopMenuResponse shopMenuResponse = shopService.getShopMenus(id);
        return ResponseEntity.ok(shopMenuResponse);
    }

    @GetMapping("/shops/{shopId}/menus/categories")
    public ResponseEntity<MenuCategoriesResponse> getMenuCategories(
        @PathVariable Integer shopId
    ) {
        MenuCategoriesResponse menuCategories = shopService.getMenuCategories(shopId);
        return ResponseEntity.ok(menuCategories);
    }

    @GetMapping("/shops/{id}")
    public ResponseEntity<ShopResponse> getShopById(
        @PathVariable Integer id
    ) {
        ShopResponse shopResponse = shopService.getShop(id);
        return ResponseEntity.ok(shopResponse);
    }

    @GetMapping("/shops")
    public ResponseEntity<ShopsResponse> getShops() {
        ShopsResponse shopsResponse = shopService.getShops();
        return ResponseEntity.ok(shopsResponse);
    }

    @GetMapping("/shops/categories")
    public ResponseEntity<ShopCategoriesResponse> getShopsCategories() {
        ShopCategoriesResponse shopCategoriesResponse = shopService.getShopsCategories();
        return ResponseEntity.ok(shopCategoriesResponse);
    }

    @GetMapping("/shops/{shopId}/events")
    public ResponseEntity<ShopEventsResponse> getShopEvents(
        @PathVariable Integer shopId
    ) {
        var response = shopService.getShopEvents(shopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shops/events")
    public ResponseEntity<ShopEventsResponse> getShopAllEvent() {
        var response = shopService.getAllEvents();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shops/{shopId}/reviews")
    public ResponseEntity<ShopReviewsResponse> getReviews(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "50", required = false) Integer limit,
        @RequestParam(name = "sorter", defaultValue = "LATEST") ReviewsSortCriteria sortBy,
        @UserId Integer userId
    ) {
        ShopReviewsResponse reviewResponse = shopReviewService.getReviewsByShopId(shopId, userId, page, limit, sortBy);
        return ResponseEntity.ok(reviewResponse);
    }

    @GetMapping("/shops/{shopId}/reviews/me")
    public ResponseEntity<ShopMyReviewsResponse> getMyReviews(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestParam(name = "sorter", defaultValue = "LATEST") ReviewsSortCriteria sortBy,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        ShopMyReviewsResponse reviewsResponse = shopReviewService.getMyReviewsByShopId(shopId, studentId, sortBy);
        return ResponseEntity.ok(reviewsResponse);
    }

    @PostMapping("/shops/{shopId}/reviews")
    public ResponseEntity<Void> createReview(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestBody @Valid CreateReviewRequest createReviewRequest,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        shopReviewService.createReview(createReviewRequest, studentId, shopId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/shops/{shopId}/reviews/{reviewId}")
    public ResponseEntity<Void> modifyReview(
        @Parameter(in = PATH) @PathVariable Integer reviewId,
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestBody @Valid ModifyReviewRequest modifyReviewRequest,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        shopReviewService.modifyReview(modifyReviewRequest, reviewId, studentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/shops/{shopId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
        @Parameter(in = PATH) @PathVariable Integer reviewId,
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        shopReviewService.deleteReview(reviewId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shops/reviews/reports/categories")
    public ResponseEntity<ShopReviewReportCategoryResponse> getReportCategory(
    ) {
        var shopReviewReportCategoryResponse = shopReviewService.getReviewReportCategories();
        return ResponseEntity.ok(shopReviewReportCategoryResponse);
    }

    @PostMapping("/shops/{shopId}/reviews/{reviewId}/reports")
    public ResponseEntity<Void> reportReview(
        @Parameter(in = PATH) @PathVariable Integer reviewId,
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestBody @Valid ShopReviewReportRequest shopReviewReportRequest,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        shopReviewService.reportReview(shopId, reviewId, studentId, shopReviewReportRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v2/shops")
    public ResponseEntity<ShopsResponseV2> getShopsV2(
        @RequestParam(name = "sorter", defaultValue = "NONE") ShopsSortCriteria sortBy,
        @RequestParam(name = "filter", required = false) List<ShopsFilterCriteria> shopsFilterCriterias,
        @RequestParam(name = "query") String query
    ) {
        if (shopsFilterCriterias == null) {
            shopsFilterCriterias = Collections.emptyList();
        }
        var shops = shopService.getShopsV2(sortBy, shopsFilterCriterias, query);
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/shops/{shopId}/reviews/{reviewId}")
    public ResponseEntity<ShopReviewResponse> getReview(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @Parameter(in = PATH) @PathVariable Integer reviewId
    ) {
        ShopReviewResponse shopReviewResponse = shopReviewService.getReviewByReviewId(shopId, reviewId);
        return ResponseEntity.ok(shopReviewResponse);
    }

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            }
    )
    @Operation(summary = "주변상점 검색어에 따른 연관검색어 조회")
    @GetMapping("/search/related/{prefix}")
    public ResponseEntity<RelatedKeyword> getRelatedKeyword(
            @PathVariable(name = "prefix") String prefix
    ) {
        return ResponseEntity.ok(searchService.getRelatedKeywordByQuery(prefix));
    }
}
