package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.shop.dto.review.AdminModifyShopReviewReportStatusRequest;
import in.koreatech.koin.admin.shop.dto.review.AdminShopsReviewsResponse;
import in.koreatech.koin.admin.shop.service.AdminShopReviewService;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminShopReviewController implements AdminShopReviewApi {

    private final AdminShopReviewService adminShopReviewService;

    @GetMapping("/admin/shops/reviews")
    public ResponseEntity<AdminShopsReviewsResponse> getReviews(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_reported", required = false) Boolean isReported,
        @RequestParam(name = "has_unhandled_report", required = false) Boolean hasUnhandledReport,
        @RequestParam(name = "shop_id", required = false) Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopsReviewsResponse response = adminShopReviewService.getReviews(
            page, limit, isReported, hasUnhandledReport, shopId
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/admin/shops/reviews/{id}")
    public ResponseEntity<Void> modifyReviewReportStatus(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminModifyShopReviewReportStatusRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopReviewService.modifyShopReviewReportStatus(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/admin/shops/reviews/{id}")
    public ResponseEntity<Void> deleteReview(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopReviewService.deleteShopReview(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
