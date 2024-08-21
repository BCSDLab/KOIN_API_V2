package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.domain.shop.dto.ShopReviewReportRequest.InnerShopReviewReport;
import static in.koreatech.koin.domain.shop.model.ReportStatus.UNHANDLED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.dto.CreateReviewRequest;
import in.koreatech.koin.domain.shop.dto.ModifyReviewRequest;
import in.koreatech.koin.domain.shop.dto.ReviewsSortCriteria;
import in.koreatech.koin.domain.shop.dto.ShopMyReviewsResponse;
import in.koreatech.koin.domain.shop.dto.ShopReviewReportCategoryResponse;
import in.koreatech.koin.domain.shop.dto.ShopReviewReportRequest;
import in.koreatech.koin.domain.shop.dto.ShopReviewResponse;
import in.koreatech.koin.domain.shop.dto.ShopReviewsResponse;
import in.koreatech.koin.domain.shop.exception.ReviewNotFoundException;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ShopReviewImage;
import in.koreatech.koin.domain.shop.model.ShopReviewMenu;
import in.koreatech.koin.domain.shop.model.ShopReviewReport;
import in.koreatech.koin.domain.shop.model.ShopReviewReportCategory;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewMenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.model.Criteria;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopReviewService {

    private final ShopRepository shopRepository;
    private final ShopReviewRepository shopReviewRepository;
    private final ShopReviewImageRepository shopReviewImageRepository;
    private final ShopReviewMenuRepository shopReviewMenuRepository;
    private final ShopReviewReportRepository shopReviewReportRepository;
    private final ShopReviewReportCategoryRepository shopReviewReportCategoryRepository;
    private final StudentRepository studentRepository;

    private final EntityManager entityManager;

    public ShopReviewsResponse getReviewsByShopId(
        Integer shopId,
        Integer userId,
        Integer page,
        Integer limit,
        ReviewsSortCriteria sortBy
    ) {
        Integer total = shopReviewRepository.countByShopIdAndIsDeletedFalse(shopId);
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            sortBy.getSort()
        );
        Page<ShopReview> result = shopReviewRepository.findByShopIdAndIsDeletedFalse(
            shopId,
            pageRequest
        );
        Map<Integer, Integer> ratings = getRating(shopId);
        return ShopReviewsResponse.from(result, userId, criteria, ratings);
    }

    @Transactional
    public void createReview(CreateReviewRequest createReviewRequest, Integer studentId, Integer shopId) {
        epository.getById(studentId);

        Student student = studentRepository.getById(studentId);
        Shop shop = shopRepository.getById(shopId);
        ShopReview shopReview = ShopReview.builder()
            .reviewer(student)
            .content(createReviewRequest.content())
            .rating(createReviewRequest.rating())
            .shop(shop)
            .build();
        ShopReview savedShopReview = shopReviewRepository.save(shopReview);
        for (String imageUrl : createReviewRequest.imageUrls()) {
            shopReviewImageRepository.save(ShopReviewImage.builder()
                .review(savedShopReview)
                .imageUrls(imageUrl)
                .build());
        }
        for (String menuName : createReviewRequest.menuNames()) {
            shopReviewMenuRepository.save(ShopReviewMenu.builder()
                .review(savedShopReview)
                .menuName(menuName)
                .build());
        }
    }

    @Transactional
    public void deleteReview(Integer reviewId, Integer studentId) {
        ShopReview shopReview = shopReviewRepository.getByIdAndIsDeleted(reviewId);
        if (!Objects.equals(shopReview.getReviewer().getId(), studentId)) {
            throw AuthenticationException.withDetail("해당 유저가 작성한 리뷰가 아닙니다.");
        }
        shopReview.deleteReview();
    }

    @Transactional
    public void modifyReview(ModifyReviewRequest modifyReviewRequest, Integer reviewId, Integer studentId) {
        ShopReview shopReview = shopReviewRepository.getByIdAndIsDeleted(reviewId);
        if (!Objects.equals(shopReview.getReviewer().getId(), studentId)) {
            throw AuthenticationException.withDetail("해당 유저가 작성한 리뷰가 아닙니다.");
        }
        shopReview.modifyReview(
            modifyReviewRequest.content(),
            modifyReviewRequest.rating()
        );
        shopReview.modifyReviewImage(modifyReviewRequest.imageUrls(), entityManager);
        shopReview.modifyMenuName(modifyReviewRequest.menuNames(), entityManager);
    }

    @Transactional
    public void reportReview(
        Integer shopId,
        Integer reviewId,
        Integer studentId,
        ShopReviewReportRequest shopReviewReportRequest
    ) {
        Student student = studentRepository.getById(studentId);
        ShopReview shopReview = shopReviewRepository.getAllByIdAndShopIdAndIsDeleted(reviewId, shopId);
        for (InnerShopReviewReport shopReviewReport : shopReviewReportRequest.reports()) {
            shopReviewReportRepository.save(
                ShopReviewReport.builder()
                    .userId(student)
                    .review(shopReview)
                    .title(shopReviewReport.title())
                    .reportStatus(UNHANDLED)
                    .content(shopReviewReport.content())
                    .build()
            );
        }
    }

    public ShopReviewReportCategoryResponse getReviewReportCategories() {
        List<ShopReviewReportCategory> shopReviewReportCategories = shopReviewReportCategoryRepository.findAll();
        return ShopReviewReportCategoryResponse.from(shopReviewReportCategories);
    }

    private Map<Integer, Integer> getRating(Integer shopId) {
        Map<Integer, Integer> ratings = new HashMap<>(Map.of(
            1, 0,
            2, 0,
            3, 0,
            4, 0,
            5, 0
        ));
        for (Integer rating : ratings.keySet()) {
            Integer count = shopReviewRepository.countByShopIdAndRatingAndIsDeletedFalse(shopId, rating);
            ratings.put(rating, count);
        }
        return ratings;
    }

    public ShopReviewResponse getReviewByReviewId(Integer shopId, Integer reviewId) {
        ShopReview shopReview = shopReviewRepository.getByIdAndIsDeleted(reviewId);
        if (!Objects.equals(shopReview.getShop().getId(), shopId)) {
            throw ReviewNotFoundException.withDetail("해당 상점의 리뷰가 아닙니다.");
        }
        return ShopReviewResponse.from(shopReview);
    }

    public ShopMyReviewsResponse getMyReviewsByShopId(Integer shopId, Integer studentId, ReviewsSortCriteria sortBy) {
        List<ShopReview> reviews = shopReviewRepository.findByShopIdAndReviewerIdAndIsDeletedFalse(
            shopId,
            studentId,
            sortBy.getSort()
        );
        return ShopMyReviewsResponse.from(reviews);
    }
}
