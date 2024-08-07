package in.koreatech.koin.domain.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.MenuImage;

public interface MenuImageRepository extends Repository<MenuImage, Integer> {

    MenuImage save(MenuImage menuImage);
}
