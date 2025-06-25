package in.koreatech.koin.domain.order.address.controller;

import static in.koreatech.koin.domain.user.model.UserType.GENERAL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.order.address.dto.AddressSearchRequest;
import in.koreatech.koin.domain.order.address.dto.AddressSearchResponse;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressResponse;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressRequestFilter;
import in.koreatech.koin.domain.order.address.dto.RiderMessageResponse;
import in.koreatech.koin.domain.order.address.dto.UserCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.address.dto.UserOffCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.address.dto.UserDeliveryAddressResponse;
import in.koreatech.koin.domain.order.address.service.AddressOpenApiService;
import in.koreatech.koin.domain.order.address.service.AddressService;
import in.koreatech.koin.domain.order.address.service.CampusAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AddressController implements AddressApi {

    private final AddressService addressService;
    private final AddressOpenApiService addressOpenApiService;
    private final CampusAddressService campusAddressService;

    @GetMapping("/address/search")
    public ResponseEntity<AddressSearchResponse> searchAddress(
        @ParameterObject @Valid AddressSearchRequest request
    ) {
        AddressSearchResponse response = addressOpenApiService.searchAddress(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/address/delivery/campus")
    public ResponseEntity<CampusDeliveryAddressResponse> getCampusAddresses(
        @RequestParam(name = "filter", defaultValue = "ALL") CampusDeliveryAddressRequestFilter filter
    ) {
        CampusDeliveryAddressResponse response = campusAddressService.getCampusDeliveryAddresses(filter);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/address/delivery/user/off-campus")
    public ResponseEntity<UserDeliveryAddressResponse> addOffCampusDeliveryAddress(
        @RequestBody @Valid UserOffCampusDeliveryAddressRequest request,
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        UserDeliveryAddressResponse response = addressService.addOffCampusDeliveryAddress(
            request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/address/delivery/user/campus")
    public ResponseEntity<UserDeliveryAddressResponse> addCampusDeliveryAddress(
        @RequestBody @Valid UserCampusDeliveryAddressRequest request,
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    ) {
        UserDeliveryAddressResponse response = addressService.addCampusDeliveryAddress(
            request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/address/delivery/rider-message")
    public ResponseEntity<RiderMessageResponse> getRiderMessages() {
        RiderMessageResponse response = addressService.getRiderMessage();
        return ResponseEntity.ok(response);
    }
}
