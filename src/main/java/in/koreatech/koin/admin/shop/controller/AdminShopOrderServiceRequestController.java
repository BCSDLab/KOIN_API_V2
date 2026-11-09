package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.shop.dto.shop.AdminShopOrderServiceRequestsResponse;
import in.koreatech.koin.admin.shop.service.AdminShopOrderServiceRequestService;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminShopOrderServiceRequestController implements AdminShopOrderServiceRequestApi {

    private final AdminShopOrderServiceRequestService requestService;

    @GetMapping("/admin/shops/order-service-requests")
    public ResponseEntity<AdminShopOrderServiceRequestsResponse> getRequests(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer limit,
        @RequestParam(required = false) ShopOrderServiceRequestStatus status,
        @RequestParam(required = false) String keyword,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopOrderServiceRequestsResponse response = requestService.getRequests(page, limit, status, keyword);
        return ResponseEntity.ok(response);
    }
}
