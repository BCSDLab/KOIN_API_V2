package in.koreatech.koin.domain.order.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.shop.dto.OwnerOrderableShopOpenStatusRequest;
import in.koreatech.koin.domain.order.shop.dto.OwnerOrderableShopsResponse;
import in.koreatech.koin.domain.order.shop.service.OwnerOrderableShopService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OwnerOrderableShopController implements OwnerOrderableShopApi {

    private final OwnerOrderableShopService ownerOrderableShopService;

    @GetMapping("/owner/order/shops")
    public ResponseEntity<OwnerOrderableShopsResponse> getOrderableShops(
        @Auth(permit = {OWNER}) Integer ownerId
    ) {
        OwnerOrderableShopsResponse response = ownerOrderableShopService.getOrderableShops(ownerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/owner/order/shop/{orderableShopId}/open")
    public ResponseEntity<Void> changeOpenStatus(
        @PathVariable Integer orderableShopId,
        @RequestBody @Valid OwnerOrderableShopOpenStatusRequest request,
        @Auth(permit = {OWNER}) Integer ownerId
    ) {
        ownerOrderableShopService.changeOpenStatus(ownerId, orderableShopId, request.isOpen());
        return ResponseEntity.ok().build();
    }
}
