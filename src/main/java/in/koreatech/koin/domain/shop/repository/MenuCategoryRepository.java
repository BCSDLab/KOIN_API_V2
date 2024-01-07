package in.koreatech.koin.domain.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.MenuCategory;

public interface MenuCategoryRepository extends Repository<MenuCategory, Long> {
    List<MenuCategory> findAllByShopId(Long shopId);
}
