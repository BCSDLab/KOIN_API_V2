package in.koreatech.koin.admin.shop.repository;

import in.koreatech.koin.domain.shop.model.ShopReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminShopReviewCustomRepository {
    Page<ShopReview> findShopReview(Integer shopId, Boolean isReported, Boolean isHaveUnhandledReport, Pageable pageable);
    Long countShopReview(Integer shopId, Boolean isReported, Boolean isHaveUnhandledReport);
}
