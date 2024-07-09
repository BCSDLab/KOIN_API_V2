package in.koreatech.koin.domain.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopReviewReport;

public interface ShopReviewReportRepository extends Repository<ShopReviewReport, Integer> {

    ShopReviewReport save(ShopReviewReport shopReviewReport);
}
