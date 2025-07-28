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
import in.koreatech.koin.domain.shop.service.ShopReviewService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
public class ShopReviewController implements ShopReviewApi {

    private final ShopReviewService shopReviewService;

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
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    ) {
        ShopMyReviewsResponse reviewsResponse = shopReviewService.getMyReviewsByShopId(shopId, studentId, sortBy);
        return ResponseEntity.ok(reviewsResponse);
    }

    @PostMapping("/shops/{shopId}/reviews")
    public ResponseEntity<Void> createReview(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestBody @Valid CreateReviewRequest createReviewRequest,
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    ) {
        shopReviewService.createReview(createReviewRequest, studentId, shopId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/shops/{shopId}/reviews/{reviewId}")
    public ResponseEntity<Void> modifyReview(
        @Parameter(in = PATH) @PathVariable Integer reviewId,
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @RequestBody @Valid ModifyReviewRequest modifyReviewRequest,
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    ) {
        shopReviewService.modifyReview(modifyReviewRequest, reviewId, studentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/shops/{shopId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
        @Parameter(in = PATH) @PathVariable Integer reviewId,
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
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
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    ) {
        shopReviewService.reportReview(shopId, reviewId, studentId, shopReviewReportRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shops/{shopId}/reviews/{reviewId}")
    public ResponseEntity<ShopReviewResponse> getReview(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @Parameter(in = PATH) @PathVariable Integer reviewId
    ) {
        ShopReviewResponse shopReviewResponse = shopReviewService.getReviewByReviewId(shopId, reviewId);
        return ResponseEntity.ok(shopReviewResponse);
    }
}
