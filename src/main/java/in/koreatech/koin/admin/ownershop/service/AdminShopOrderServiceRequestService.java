package in.koreatech.koin.admin.ownershop.service;

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
import in.koreatech.koin.domain.order.shop.model.entity.delivery.OrderableShopDeliveryTip;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
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
    private final OrderableShopRepository orderableShopRepository;

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
        ShopOrderServiceRequest shopOrderServiceRequest = adminShopOrderServiceRequestRepository
            .getByIdWithShopAndOwner(id);
        if (shopOrderServiceRequest.getRequestStatus() != PENDING) {
            throw CustomException.of(NOT_PENDING_REQUEST);
        }
        shopOrderServiceRequest.approve();

        createOrderableShopFromRequest(shopOrderServiceRequest);
    }

    private void createOrderableShopFromRequest(ShopOrderServiceRequest shopOrderServiceRequest) {
        // Shop의 계좌 정보 업데이트
        Shop shop = shopOrderServiceRequest.getShop();
        shop.updateBankAndAccount(shopOrderServiceRequest.getBank(), shopOrderServiceRequest.getAccountNumber());

        // OrderableShop 생성 또는 업데이트
        OrderableShop orderableShop = createOrderableShop(shop, shopOrderServiceRequest);
        createOrderableShopDeliveryOption(shopOrderServiceRequest, orderableShop);
        createDeliveryTips(orderableShop, shopOrderServiceRequest);
        orderableShopRepository.save(orderableShop);
        createOwnerAttachments(shop, shopOrderServiceRequest);
    }

    private OrderableShop createOrderableShop(Shop shop, ShopOrderServiceRequest shopOrderServiceRequest) {
        OrderableShop orderableShop;
        if (orderableShopRepository.existsByShopId(shop.getId())) {
            orderableShop = orderableShopRepository.getByShopId(shop.getId());
            orderableShop.updateOrderableShop(
                shopOrderServiceRequest.getMinimumOrderAmount(),
                shopOrderServiceRequest.getIsTakeout()
            );
        } else {
            orderableShop = OrderableShop.builder()
                .shop(shop)
                .minimumOrderAmount(shopOrderServiceRequest.getMinimumOrderAmount())
                .takeout(shopOrderServiceRequest.getIsTakeout())
                .build();
        }
        return orderableShop;
    }

    private void createOrderableShopDeliveryOption(
        ShopOrderServiceRequest shopOrderServiceRequest,
        OrderableShop orderableShop
    ) {
        ShopOrderServiceRequestDeliveryOption deliveryOption = shopOrderServiceRequest.getDeliveryOption();
        boolean isCampusDelivery = deliveryOption == CAMPUS || deliveryOption == BOTH;
        boolean isOffCampusDelivery = deliveryOption == OFF_CAMPUS || deliveryOption == BOTH;

        if (orderableShop.getDeliveryOption() != null) {
            orderableShop.getDeliveryOption().updateDeliveryOption(isCampusDelivery, isOffCampusDelivery);
        } else {
            OrderableShopDeliveryOption orderableShopDeliveryOption = OrderableShopDeliveryOption.builder()
                .orderableShop(orderableShop)
                .campusDelivery(isCampusDelivery)
                .offCampusDelivery(isOffCampusDelivery)
                .build();
            orderableShop.updateDeliveryOption(orderableShopDeliveryOption);
        }
    }

    private void createDeliveryTips(OrderableShop orderableShop, ShopOrderServiceRequest shopOrderServiceRequest) {
        if (orderableShop.getDeliveryTip() != null) {
            orderableShop.getDeliveryTip().updateDeliveryTip(
                shopOrderServiceRequest.getCampusDeliveryTip(),
                shopOrderServiceRequest.getOffCampusDeliveryTip()
            );
        } else {
            OrderableShopDeliveryTip deliveryTip = OrderableShopDeliveryTip.builder()
                .campusDeliveryTip(shopOrderServiceRequest.getCampusDeliveryTip())
                .offCampusDeliveryTip(shopOrderServiceRequest.getOffCampusDeliveryTip())
                .orderableShop(orderableShop)
                .build();
            orderableShop.updateDeliveryTip(deliveryTip);
        }
    }

    private void createOwnerAttachments(Shop shop, ShopOrderServiceRequest shopOrderServiceRequest) {
        Owner owner = shop.getOwner();
        // 사업자 등록증 URL
        OwnerAttachment businessLicenseAttachment = OwnerAttachment.builder()
            .url(shopOrderServiceRequest.getBusinessLicenseUrl())
            .owner(owner)
            .isDeleted(false)
            .build();
        owner.getAttachments().add(businessLicenseAttachment);

        // 영업 신고증 URL
        OwnerAttachment businessCertificateAttachment = OwnerAttachment.builder()
            .url(shopOrderServiceRequest.getBusinessCertificateUrl())
            .owner(owner)
            .isDeleted(false)
            .build();
        owner.getAttachments().add(businessCertificateAttachment);

        // 통장 사본 URL
        OwnerAttachment bankCopyAttachment = OwnerAttachment.builder()
            .url(shopOrderServiceRequest.getBankCopyUrl())
            .owner(owner)
            .isDeleted(false)
            .build();
        owner.getAttachments().add(bankCopyAttachment);
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
