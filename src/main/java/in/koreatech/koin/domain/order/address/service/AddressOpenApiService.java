package in.koreatech.koin.domain.order.address.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.order.address.dto.AddressSearchRequest;
import in.koreatech.koin.domain.order.address.dto.AddressSearchResponse;
import in.koreatech.koin.domain.order.address.dto.RoadNameAddressApiResponse;
import in.koreatech.koin.domain.order.address.service.client.RoadNameAddressClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressOpenApiService {

    private final RoadNameAddressClient roadNameAddressClient;

    public AddressSearchResponse searchAddress(AddressSearchRequest request) {
        RoadNameAddressApiResponse apiResponse = roadNameAddressClient.searchAddress(
            request.keyword(),
            request.currentPage(),
            request.countPerPage()
        );

        return AddressSearchResponse.from(apiResponse);
    }
}
