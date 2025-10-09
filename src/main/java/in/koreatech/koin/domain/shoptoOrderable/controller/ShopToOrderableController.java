package in.koreatech.koin.domain.shoptoOrderable.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.shoptoOrderable.dto.ShopToOrderableRequest;
import in.koreatech.koin.domain.shoptoOrderable.service.ShopToOrderableService;
import lombok.RequiredArgsConstructor;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;

//Todo: ShopToOrderable 이라는 명칭은 임시임 추후 변경
@RestController
@RequiredArgsConstructor
public class ShopToOrderableController implements ShopToOrderableApi {
    private final ShopToOrderableService shopToOrderableService;

    @PostMapping("/owner/shops/orderable-requests")
    public ResponseEntity<Void> createOrderableRequest(
        @Auth(permit = {OWNER}) Integer ownerId,
        @RequestBody @Valid ShopToOrderableRequest request
    ) {
        shopToOrderableService.createOrderableRequest(ownerId, request);
        return ResponseEntity.status(201).build();
    }
}
