package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.shop.repository.shop.AdminShopNotificationMessageRepository;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class ShopNotificationMessageAcceptanceFixture {

    private final AdminShopNotificationMessageRepository adminShopNotificationMessageRepository;

    public ShopNotificationMessageAcceptanceFixture(
        AdminShopNotificationMessageRepository adminShopNotificationMessageRepository
    ) {
        this.adminShopNotificationMessageRepository = adminShopNotificationMessageRepository;
    }

    public ShopNotificationMessage 알림메시지_가게() {
        return adminShopNotificationMessageRepository.save(
            ShopNotificationMessage.builder()
                .title(",맛있게 드셨나요?")
                .content("가게에 리뷰를 남겨보세요!")
                .build()
        );
    }

    public ShopNotificationMessage 알림메시지_콜벤() {
        return adminShopNotificationMessageRepository.save(
            ShopNotificationMessage.builder()
                .title(",편안히 이용하셨나요?")
                .content("리뷰를 남겨보세요!")
                .build()
        );
    }
}
