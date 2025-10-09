package in.koreatech.koin.domain.shoptoOrderable.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.shoptoOrderable.dto.ShopToOrderableRequest;
import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderable;
import in.koreatech.koin.domain.shoptoOrderable.repository.ShopToOrderableRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import in.koreatech.koin.global.exception.CustomException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShopToOrderableService {
    private final ShopToOrderableRepository shopToOrderableRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public void createOrderableRequest(Integer ownerId, ShopToOrderableRequest request) {
        Shop shop = shopRepository.findById(request.shopId())
            .orElseThrow(
                () -> CustomException.of(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP, "shopId: " + request.shopId()));

        // 이미 신청한 내역이 있는지 확인
        if (shopToOrderableRepository.existsByShopId(request.shopId())) {
            throw CustomException.of(ApiResponseCode.ALREADY_REQUESTED_ORDERABLE_SHOP, "shopId: " + request.shopId());
        }

        ShopToOrderable shopToOrderable = ShopToOrderable.builder()
            .shop(shop)
            .minimumOrderAmount(request.minimumOrderAmount())
            .takeout(request.takeout())
            .deliveryOption(request.deliveryOption())
            .campusDeliveryTip(request.campusDeliveryTip())
            .outsideDeliveryTip(request.outsideDeliveryTip())
            .isOpen(request.isOpen())
            .businessLicenseUrl(request.businessLicenseUrl())
            .requestStatus(ShopToOrderable.RequestStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        shopToOrderableRepository.save(shopToOrderable);
    }
}
