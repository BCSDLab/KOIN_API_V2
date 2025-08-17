package in.koreatech.koin.admin.shop.service;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.DELETED;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.dto.review.AdminModifyShopReviewReportStatusRequest;
import in.koreatech.koin.admin.shop.dto.review.AdminShopsReviewsResponse;
import in.koreatech.koin.admin.shop.repository.review.AdminShopReviewCustomRepository;
import in.koreatech.koin.admin.shop.repository.review.AdminShopReviewRepository;
import in.koreatech.koin.domain.shop.cache.aop.RefreshShopsCache;
import in.koreatech.koin.domain.shop.exception.ReviewNotFoundException;
import in.koreatech.koin.domain.shop.model.review.ReportStatus;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReviewReport;
import in.koreatech.koin.common.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminShopReviewService {

    private final AdminShopReviewRepository adminShopReviewRepository;
    private final AdminShopReviewCustomRepository adminShopReviewCustomRepository;

    public AdminShopsReviewsResponse getReviews(
        Integer page,
        Integer limit,
        Boolean isReported,
        Boolean hasUnhandledReport,
        Integer shopId
    ) {
        Long reviewCount = adminShopReviewCustomRepository.countShopReview(shopId, isReported, hasUnhandledReport);
        Criteria criteria = Criteria.of(page, limit, reviewCount.intValue());
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id")
        );

        Page<ShopReview> reviews = adminShopReviewCustomRepository.findShopReview(
            shopId,
            isReported,
            hasUnhandledReport,
            pageRequest
        );
        return AdminShopsReviewsResponse.of(reviews, criteria);
    }

    @Transactional
    @RefreshShopsCache
    public void modifyShopReviewReportStatus(Integer reviewId, AdminModifyShopReviewReportStatusRequest request) {
        ShopReview shopReview = adminShopReviewRepository.findById(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.withDetail("해당 리뷰를 찾을 수 없습니다.: " + reviewId));

        List<ShopReviewReport> unhandledReports = shopReview.getReports().stream()
            .filter(report -> report.getReportStatus() == ReportStatus.UNHANDLED)
            .toList();

        unhandledReports.forEach(report -> report.modifyReportStatus(request.reportStatus()));
    }

    @Transactional
    @RefreshShopsCache
    public void deleteShopReview(Integer reviewId) {
        ShopReview shopReview = adminShopReviewRepository.findById(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.withDetail("해당 리뷰를 찾을 수 없습니다.: " + reviewId));

        shopReview.getReports().forEach(report -> report.modifyReportStatus(DELETED));
        shopReview.deleteReview();
    }
}
