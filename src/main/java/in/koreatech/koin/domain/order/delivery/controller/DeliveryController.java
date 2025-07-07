package in.koreatech.koin.domain.order.delivery.controller;

import static in.koreatech.koin.domain.user.model.UserType.GENERAL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.order.delivery.dto.RiderMessageResponse;
import in.koreatech.koin.domain.order.delivery.dto.UserCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.delivery.dto.UserDeliveryAddressResponse;
import in.koreatech.koin.domain.order.delivery.dto.UserOffCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.delivery.dto.UserOffCampusDeliveryAddressValidateRequest;
import in.koreatech.koin.domain.order.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    @PostMapping("/delivery/address/off-campus")
    public ResponseEntity<UserDeliveryAddressResponse> addOffCampusDeliveryAddress(
        @RequestBody @Valid UserOffCampusDeliveryAddressRequest request,
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        UserDeliveryAddressResponse response = deliveryService.addOffCampusDeliveryAddress(
            request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delivery/address/campus")
    public ResponseEntity<UserDeliveryAddressResponse> addCampusDeliveryAddress(
        @RequestBody @Valid UserCampusDeliveryAddressRequest request,
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        UserDeliveryAddressResponse response = deliveryService.addCampusDeliveryAddress(
            request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delivery/address/off-campus/validate")
    public ResponseEntity<Void> validateOffCampusDeliveryAddress(
        @RequestBody @Valid UserOffCampusDeliveryAddressValidateRequest request
    ) {
        deliveryService.validateOffCampusAddress(request.toOffCampusAddress());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/delivery/rider-message")
    public ResponseEntity<RiderMessageResponse> getRiderMessages() {
        RiderMessageResponse response = deliveryService.getRiderMessage();
        return ResponseEntity.ok(response);
    }
}
