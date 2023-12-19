package in.koreatech.koin.repository.shop;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.MenuCategory;

public interface MenuCategoryRepository extends Repository<MenuCategory, Long> {
    MenuCategory save(MenuCategory menuCategory);
}
