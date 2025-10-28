package in.koreatech.koin.domain.shoptoOrderable.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.shoptoOrderable.dto.ShopToOrderableRequest;
import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderable;
import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderableRequestStatus;
import in.koreatech.koin.domain.shoptoOrderable.repository.ShopToOrderableRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import in.koreatech.koin.global.exception.CustomException;

@Service
@RequiredArgsConstructor
public class ShopToOrderableService {

    private final ShopToOrderableRepository shopToOrderableRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public void createOrderableRequest(Integer ownerId, ShopToOrderableRequest request, Integer shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(
                () -> CustomException.of(ApiResponseCode.NOT_FOUND_SHOP, "shopId: " + shopId));

        validateCreateOrderableRequest(ownerId, shopId, shop);

        ShopToOrderable shopToOrderable = ShopToOrderable.builder()
            .shop(shop)
            .minimumOrderAmount(request.minimumOrderAmount())
            .isTakeout(request.isTakeout())
            .deliveryOption(request.deliveryOption())
            .campusDeliveryTip(request.campusDeliveryTip())
            .offCampusDeliveryTip(request.offCampusDeliveryTip())
            .businessLicenseUrl(request.businessLicenseUrl())
            .businessCertificateUrl(request.businessCertificateUrl())
            .bankCopyUrl(request.bankCopyUrl())
            .bank(request.bank())
            .accountNumber(request.accountNumber())
            .build();

        shopToOrderableRepository.save(shopToOrderable);
    }

    private void validateCreateOrderableRequest(
        Integer ownerId,
        Integer shopId,
        Shop shop
    ) {
        // 이미 신청한 내역이 있는지 확인
        if (shopToOrderableRepository.existsByShopIdAndRequestStatus(shopId, ShopToOrderableRequestStatus.PENDING)) {
            throw CustomException.of(ApiResponseCode.DUPLICATE_REQUESTED_ORDERABLE_SHOP, "shopId: " + shopId);
        }

        // 가게 사장님인지 확인
        if (!shop.getOwner().getId().equals(ownerId)) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_SHOP_OWNER,
                "ownerId: " + ownerId + ", shopId: " + shopId);
        }

        // 이미 주문가능 상점인지 확인
        if (shopToOrderableRepository.existsByShopIdAndRequestStatus(shopId, ShopToOrderableRequestStatus.APPROVED)) {
            throw CustomException.of(ApiResponseCode.DUPLICATE_ORDERABLE_SHOP, "shopId: " + shopId);
        }
    }
}
