package in.koreatech.koin.domain.shop.repository.menu;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.menu.MenuImage;

public interface MenuImageRepository extends Repository<MenuImage, Integer> {

    MenuImage save(MenuImage menuImage);
}
