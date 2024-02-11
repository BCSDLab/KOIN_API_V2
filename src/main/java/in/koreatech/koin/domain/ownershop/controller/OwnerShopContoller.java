package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.ownershop.service.OwnerShopService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OwnerShopContoller implements OwnerShopApi {

    private final OwnerShopService ownerShopService;

    @Override
    public ResponseEntity<OwnerShopsResponse> getOwnerShops(@Auth(permit = {OWNER}) Long ownerId) {
        OwnerShopsResponse ownerShopsResponses = ownerShopService.getOwnerShops(ownerId);
        return ResponseEntity.ok().body(ownerShopsResponses);
    }
}
