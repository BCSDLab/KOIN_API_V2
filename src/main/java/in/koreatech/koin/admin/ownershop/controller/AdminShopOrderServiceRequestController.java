package in.koreatech.koin.admin.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceResponse;
import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServicesResponse;
import in.koreatech.koin.admin.ownershop.dto.ShopOrderServiceRequestCondition;
import in.koreatech.koin.admin.ownershop.service.AdminShopOrderServiceRequestService;
import in.koreatech.koin.global.auth.Auth;
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

    @GetMapping("/admin/owner/shops/order-service-requests/{orderServiceRequestId}")
    public ResponseEntity<AdminShopOrderServiceResponse> getOrderServiceRequest(
        @PathVariable("orderServiceRequestId") Integer orderServiceRequestId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopOrderServiceResponse response = adminShopOrderServiceRequestService.getOrderServiceRequest(
            orderServiceRequestId);
        return ResponseEntity.ok().body(response);
    }
}
