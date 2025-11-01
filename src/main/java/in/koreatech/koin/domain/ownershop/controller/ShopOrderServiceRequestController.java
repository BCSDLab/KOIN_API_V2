package in.koreatech.koin.domain.ownershop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.ownershop.dto.ShopOrderServiceRequestRequest;
import in.koreatech.koin.domain.ownershop.service.ShopOrderServiceRequestService;
import in.koreatech.koin.global.duplicate.DuplicateGuard;
import lombok.RequiredArgsConstructor;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class ShopOrderServiceRequestController implements ShopOrderServiceRequestApi {

    private final ShopOrderServiceRequestService ShopOrderServiceRequestService;

    @PostMapping("/owner/shops/{shopId}/order-service-requests")
    @DuplicateGuard(key = "#ownerId + ':' + #shopId + ':' + #request.toString()", timeoutSeconds = 300)
    public ResponseEntity<Void> createOrderableRequest(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable Integer shopId,
        @RequestBody @Valid ShopOrderServiceRequestRequest request
    ) {
        ShopOrderServiceRequestService.createOrderableRequest(ownerId, request, shopId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
