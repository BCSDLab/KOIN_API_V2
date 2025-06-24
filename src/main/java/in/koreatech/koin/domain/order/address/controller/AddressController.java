package in.koreatech.koin.domain.order.address.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.address.dto.AddressSearchRequest;
import in.koreatech.koin.domain.order.address.dto.AddressSearchResponse;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressResponse;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressRequestFilter;
import in.koreatech.koin.domain.order.address.service.AddressService;
import in.koreatech.koin.domain.order.address.service.CampusAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AddressController implements AddressApi {

    private final AddressService addressService;
    private final CampusAddressService campusAddressService;

    @GetMapping("/address/search")
    public ResponseEntity<AddressSearchResponse> searchAddress(
        @ParameterObject @Valid AddressSearchRequest request
    ) {
        AddressSearchResponse response = addressService.searchAddress(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/address/delivery/campus")
    public ResponseEntity<CampusDeliveryAddressResponse> getCampusAddresses(
        @RequestParam(name = "filter", defaultValue = "ALL") CampusDeliveryAddressRequestFilter filter
    ) {
        CampusDeliveryAddressResponse response = campusAddressService.getCampusDeliveryAddresses(filter);
        return ResponseEntity.ok(response);
    }
}
