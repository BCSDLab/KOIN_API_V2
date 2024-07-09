package in.koreatech.koin.admin.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.MenuImage;

public interface AdminMenuImageRepository extends Repository<MenuImage, Integer> {

    MenuImage save(MenuImage menuImage);
}
