package in.koreatech.koin.admin.ownershop.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.SHOP_ORDER_REQUEST;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceRequestProcessRequest;
import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceResponse;
import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServicesResponse;
import in.koreatech.koin.admin.ownershop.dto.ShopOrderServiceRequestCondition;
import in.koreatech.koin.admin.ownershop.service.AdminShopOrderServiceRequestService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminShopOrderServiceRequestController implements AdminShopOrderServiceRequestApi {

    private final AdminShopOrderServiceRequestService adminShopOrderServiceRequestService;

    @GetMapping("/admin/owner/shops/order-service-requests")
    public ResponseEntity<AdminShopOrderServicesResponse> getOrderServiceRequests(
        @ParameterObject @ModelAttribute ShopOrderServiceRequestCondition shopOrderServiceRequestCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopOrderServicesResponse response = adminShopOrderServiceRequestService.getOrderServiceRequests(
            shopOrderServiceRequestCondition);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/admin/owner/shops/order-service-requests/{id}")
    public ResponseEntity<AdminShopOrderServiceResponse> getOrderServiceRequest(
        @PathVariable("id") Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopOrderServiceResponse response = adminShopOrderServiceRequestService.getOrderServiceRequest(id);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/admin/owner/shops/order-service-requests/{id}/process")
    @AdminActivityLogging(domain = SHOP_ORDER_REQUEST, domainIdParam = "id")
    public ResponseEntity<Void> processOrderServiceRequest(
        @PathVariable("id") Integer id,
        @RequestBody @Valid AdminShopOrderServiceRequestProcessRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopOrderServiceRequestService.processOrderServiceRequest(id, request);
        return ResponseEntity.ok().build();
    }
}
