package in.koreatech.koin.domain.shop.repository.review;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.review.ShopReviewReport;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface ShopReviewReportRepository extends Repository<ShopReviewReport, Integer> {

    ShopReviewReport save(ShopReviewReport shopReviewReport);

    Optional<ShopReviewReport> findById(Integer id);

}
