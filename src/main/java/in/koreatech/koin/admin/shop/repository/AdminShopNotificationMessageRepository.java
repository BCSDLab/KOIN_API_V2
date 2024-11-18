package in.koreatech.koin.admin.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;

public interface AdminShopNotificationMessageRepository extends Repository<ShopNotificationMessage, Integer> {

    ShopNotificationMessage save(ShopNotificationMessage shopNotificationMessage);
}
