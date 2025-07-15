package in.koreatech.koin.domain.order.address.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.order.address.dto.AddressSearchRequest;
import in.koreatech.koin.domain.order.address.dto.AddressSearchResponse;
import in.koreatech.koin.domain.order.address.model.RoadNameAddressDocument;
import in.koreatech.koin.domain.order.address.repository.RoadNameAddressRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoadNameAddressLocalFinder {

    private final RoadNameAddressRepository addressRepository;

    public AddressSearchResponse searchAddressFromLocal(AddressSearchRequest request) {
        Pageable pageable = PageRequest.of(request.currentPage() - 1, request.countPerPage());
        Page<RoadNameAddressDocument> resultPage = addressRepository.findByKeyword(request.keyword(), pageable);
        return AddressSearchResponse.from(resultPage);
    }
}
