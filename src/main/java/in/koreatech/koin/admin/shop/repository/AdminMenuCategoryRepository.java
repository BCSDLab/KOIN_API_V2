package in.koreatech.koin.admin.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.MenuCategory;

public interface AdminMenuCategoryRepository extends Repository<MenuCategory, Integer> {

    MenuCategory save(MenuCategory menuCategory);
}
