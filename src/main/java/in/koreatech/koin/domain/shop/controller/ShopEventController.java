package in.koreatech.koin.domain.shop.controller;

import in.koreatech.koin.domain.shop.dto.event.response.ShopEventsWithBannerUrlResponse;
import in.koreatech.koin.domain.shop.dto.event.response.ShopEventsWithThumbnailUrlResponse;
import in.koreatech.koin.domain.shop.service.ShopEventService;
import in.koreatech.koin.domain.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShopEventController implements ShopEventApi {

    private final ShopEventService shopEventService;

    @GetMapping("/shops/{shopId}/events")
    public ResponseEntity<ShopEventsWithThumbnailUrlResponse> getShopEvents(
        @PathVariable Integer shopId
    ) {
        var response = shopEventService.getShopEvents(shopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shops/events")
    public ResponseEntity<ShopEventsWithBannerUrlResponse> getShopAllEvent() {
        var response = shopEventService.getAllEvents();
        return ResponseEntity.ok(response);
    }
}
