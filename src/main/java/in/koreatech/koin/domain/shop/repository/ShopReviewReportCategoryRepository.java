package in.koreatech.koin.domain.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.review.ShopReviewReportCategory;

public interface ShopReviewReportCategoryRepository extends Repository<ShopReviewReportCategory, Integer> {

    List<ShopReviewReportCategory> findAll();

    ShopReviewReportCategory save(ShopReviewReportCategory shopReviewReportCategory);
}
