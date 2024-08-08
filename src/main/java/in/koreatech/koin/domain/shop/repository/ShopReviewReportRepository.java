package in.koreatech.koin.domain.shop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopReviewReport;

public interface ShopReviewReportRepository extends Repository<ShopReviewReport, Integer> {

    ShopReviewReport save(ShopReviewReport shopReviewReport);

    Optional<ShopReviewReport> findById(Integer id);

}
