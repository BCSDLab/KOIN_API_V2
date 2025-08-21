package in.koreatech.koin.domain.shop.service;

import in.koreatech.koin.domain.shop.dto.event.response.ShopEventsWithBannerUrlResponse;
import in.koreatech.koin.domain.shop.dto.event.response.ShopEventsWithThumbnailUrlResponse;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopEventService {

    private final Clock clock;
    private final ShopRepository shopRepository;

    public ShopEventsWithThumbnailUrlResponse getShopEvents(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        return ShopEventsWithThumbnailUrlResponse.of(shop, clock);
    }

    public ShopEventsWithBannerUrlResponse getAllEvents() {
        List<Shop> shops = shopRepository.findAllWithEventArticles();
        return ShopEventsWithBannerUrlResponse.of(shops, clock);
    }
}
