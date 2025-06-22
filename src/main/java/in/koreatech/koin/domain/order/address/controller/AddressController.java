package in.koreatech.koin.domain.order.address.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.address.dto.AddressSearchRequest;
import in.koreatech.koin.domain.order.address.dto.AddressSearchResponse;
import in.koreatech.koin.domain.order.address.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AddressController implements AddressApi {

    private final AddressService addressService;

    @GetMapping("/order/address/search")
    public ResponseEntity<AddressSearchResponse> searchAddress(
        @ParameterObject @Valid AddressSearchRequest request
    ) {
        AddressSearchResponse response = addressService.searchAddress(request);
        return ResponseEntity.ok(response);
    }
}
