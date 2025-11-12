package in.koreatech.koin.domain.ownershop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.ownershop.dto.ShopOrderServiceRequestRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.repository.ShopOrderServiceRequestRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus.*;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

@Service
@RequiredArgsConstructor
public class ShopOrderServiceRequestService {

    private final ShopOrderServiceRequestRepository shopOrderServiceRequestRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public void createOrderableRequest(Integer ownerId, ShopOrderServiceRequestRequest request, Integer shopId) {
        Shop shop = shopRepository.getById(shopId);

        validateShopOwner(shop, ownerId);
        validateDuplicateRequest(shop);

        ShopOrderServiceRequest shopOrderServiceRequest = ShopOrderServiceRequest.builder()
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

        shopOrderServiceRequestRepository.save(shopOrderServiceRequest);
    }

    private void validateShopOwner(Shop shop, Integer ownerId) {
        // 가게 사장님인지 확인
        if (!shop.isOwner(ownerId)) {
            throw CustomException.of(FORBIDDEN_SHOP_OWNER, "ownerId: " + ownerId + ", shopId: " + shop.getId());
        }
    }

    private void validateDuplicateRequest(Shop shop) {
        // 이미 신청한 내역이 있는지 확인
        if (shopOrderServiceRequestRepository.existsByShopIdAndRequestStatus(shop.getId(), PENDING)) {
            throw CustomException.of(DUPLICATE_REQUESTED_ORDERABLE_SHOP, "shopId: " + shop.getId());
        }

        // 이미 주문가능 상점인지 확인
        if (shopOrderServiceRequestRepository.existsByShopIdAndRequestStatus(shop.getId(), APPROVED)) {
            throw CustomException.of(DUPLICATE_ORDERABLE_SHOP, "shopId: " + shop.getId());
        }
    }
}
