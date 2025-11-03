package in.koreatech.koin.admin.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceResponse;
import in.koreatech.koin.admin.ownershop.dto.ShopOrderServiceRequestCondition;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminShopOrderServiceRequestController {

    @GetMapping("/admin/owner/shops/order-service-requests")
    ResponseEntity<AdminShopOrderServiceResponse> getOrderServiceRequests(
        @ParameterObject @ModelAttribute ShopOrderServiceRequestCondition shopOrderServiceRequestCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().build();
    }

}
