package in.koreatech.koin.domain.order.address.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.address.dto.AddressSearchRequest;
import in.koreatech.koin.domain.order.address.dto.AddressSearchResponse;
import in.koreatech.koin.domain.order.address.dto.RoadNameAddressApiResponse;
import in.koreatech.koin.domain.order.address.service.client.RoadNameAddressClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

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
