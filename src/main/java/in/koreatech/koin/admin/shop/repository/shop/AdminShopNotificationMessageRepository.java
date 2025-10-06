package in.koreatech.koin.admin.shop.repository.shop;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminShopNotificationMessageRepository extends Repository<ShopNotificationMessage, Integer> {

    ShopNotificationMessage save(ShopNotificationMessage shopNotificationMessage);
}
