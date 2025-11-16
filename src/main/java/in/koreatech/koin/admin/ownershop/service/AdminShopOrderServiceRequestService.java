package in.koreatech.koin.admin.ownershop.service;

import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus.APPROVED;
import static in.koreatech.koin.global.code.ApiResponseCode.SEARCH_QUERY_ONLY_WHITESPACE;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceResponse;
import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServicesResponse;
import in.koreatech.koin.admin.ownershop.dto.ShopOrderServiceRequestCondition;
import in.koreatech.koin.admin.ownershop.repository.AdminShopOrderServiceRequestRepository;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.order.shop.model.entity.delivery.OrderableShopDeliveryOption;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption.*;
import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus.PENDING;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_PENDING_REQUEST;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminShopOrderServiceRequestService {

    private final AdminShopOrderServiceRequestRepository adminShopOrderServiceRequestRepository;
    private final ShopRepository shopRepository;

    public AdminShopOrderServicesResponse getOrderServiceRequests(ShopOrderServiceRequestCondition condition) {
        if (condition.isQueryBlank()) {
            throw CustomException.of(SEARCH_QUERY_ONLY_WHITESPACE);
        }

        Long totalRequests = getTotalRequestsCount(condition);
        Criteria criteria = Criteria.of(condition.page(), condition.limit(), totalRequests.intValue());
        Sort.Direction direction = condition.sort().getDirection();

        Page<ShopOrderServiceRequest> result = getOrderServiceRequestsResultPage(condition, criteria, direction);

        return AdminShopOrderServicesResponse.of(result, criteria);
    }

    public AdminShopOrderServiceResponse getOrderServiceRequest(Integer id) {
        ShopOrderServiceRequest shopOrderServiceRequest = adminShopOrderServiceRequestRepository.getById(id);
        return AdminShopOrderServiceResponse.from(shopOrderServiceRequest);
    }

    @Transactional
    public void approveOrderServiceRequest(Integer id) {
        ShopOrderServiceRequest shopOrderServiceRequest = adminShopOrderServiceRequestRepository.getById(id);
        if (shopOrderServiceRequest.getRequestStatus() != PENDING) {
            throw CustomException.of(NOT_PENDING_REQUEST);
        }
        shopOrderServiceRequest.approve();

        //createOrderableShopFromRequest(shopOrderServiceRequest);
    }

    // TODO: 미완성 메서드
    private void createOrderableShopFromRequest(ShopOrderServiceRequest shopOrderServiceRequest) {
        // Shop의 계좌 정보 업데이트
        Shop shop = shopOrderServiceRequest.getShop();
        shop.updateBankAndAccount(shopOrderServiceRequest.getBank(), shopOrderServiceRequest.getAccountNumber());

        // 새롭게 OrderableShop 생성
        OrderableShop orderableShop = OrderableShop.builder()
            .shop(shop)
            .minimumOrderAmount(shopOrderServiceRequest.getMinimumOrderAmount())
            .takeout(shopOrderServiceRequest.getIsTakeout())
            .build();

        // 새롭게 OrderableShopDeliveryOption 생성
        ShopOrderServiceRequestDeliveryOption deliveryOption = shopOrderServiceRequest.getDeliveryOption();
        boolean isCampusDelivery = deliveryOption == CAMPUS || deliveryOption == BOTH;
        boolean isOffCampusDelivery = deliveryOption == OFF_CAMPUS || deliveryOption == BOTH;
        OrderableShopDeliveryOption orderableShopDeliveryOption = OrderableShopDeliveryOption.builder()
            .orderableShop(orderableShop)
            .campusDelivery(isCampusDelivery)
            .offCampusDelivery(isOffCampusDelivery)
            .build();

        //TODO: campusDeliveryTip, offCampusDeliveryTip 처리 필요
        // ShopBaseDeliveryTip 테이블은 거리 기반으로 팁을 설정하고 있어보임

        //TODO: businessLicenseUrl, businessCertificateUrl, bankCopyUrl 처리 필요
        // OwnerAttachment로 저장 가능?
    }


    @Transactional
    public void rejectOrderServiceRequest(Integer id) {
        ShopOrderServiceRequest shopOrderServiceRequest = adminShopOrderServiceRequestRepository.getById(id);
        if (shopOrderServiceRequest.getRequestStatus() != PENDING) {
            throw CustomException.of(NOT_PENDING_REQUEST);
        }
        shopOrderServiceRequest.reject();
    }

    private Long getTotalRequestsCount(ShopOrderServiceRequestCondition condition) {
        if (condition.isQueryNotNull() && condition.isStatusNotNull()) {
            return adminShopOrderServiceRequestRepository.countByStatusAndShopName(
                condition.status(),
                condition.query()
            );
        }

        if (condition.isQueryNotNull()) {
            return adminShopOrderServiceRequestRepository.countByShopName(condition.query());
        }

        if (condition.isStatusNotNull()) {
            return adminShopOrderServiceRequestRepository.countByStatus(condition.status());
        }

        return adminShopOrderServiceRequestRepository.count();
    }

    private Page<ShopOrderServiceRequest> getOrderServiceRequestsResultPage(
        ShopOrderServiceRequestCondition condition,
        Criteria criteria,
        Sort.Direction direction
    ) {
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(direction, "createdAt")
        );

        if (condition.isQueryNotNull() && condition.isStatusNotNull()) {
            return adminShopOrderServiceRequestRepository.findPageOrderServiceRequestsByStatusAndShopName(
                condition.status(),
                condition.query(),
                pageRequest
            );
        }

        if (condition.isQueryNotNull()) {
            return adminShopOrderServiceRequestRepository.findPageOrderServiceRequestsByShopName(
                condition.query(),
                pageRequest
            );
        }

        if (condition.isStatusNotNull()) {
            return adminShopOrderServiceRequestRepository.findPageOrderServiceRequestsByStatus(
                condition.status(),
                pageRequest
            );
        }

        return adminShopOrderServiceRequestRepository.findPageOrderServiceRequests(pageRequest);
    }
}
