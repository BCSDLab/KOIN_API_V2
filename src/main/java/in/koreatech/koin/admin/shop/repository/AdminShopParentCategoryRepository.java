package in.koreatech.koin.admin.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;

public interface AdminShopParentCategoryRepository extends Repository<ShopParentCategory, Integer> {

    List<ShopParentCategory> findAll();
}
