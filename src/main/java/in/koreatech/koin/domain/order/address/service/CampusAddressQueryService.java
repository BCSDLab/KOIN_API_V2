package in.koreatech.koin.domain.order.address.service;

import static in.koreatech.koin._common.cache.CacheKey.CAMPUS_DELIVERY_ADDRESS_CACHE;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressResponse;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressRequestFilter;
import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;
import in.koreatech.koin.domain.order.address.repository.CampusDeliveryAddressRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampusAddressQueryService {

    private final CampusDeliveryAddressRepository campusDeliveryAddressRepository;

    @Cacheable(value = CAMPUS_DELIVERY_ADDRESS_CACHE, key = "#filter.getId()")
    public CampusDeliveryAddressResponse getCampusDeliveryAddresses(CampusDeliveryAddressRequestFilter filter) {
        List<CampusDeliveryAddress> addresses = filter.getCampusDeliveryAddress(campusDeliveryAddressRepository);
        return CampusDeliveryAddressResponse.from(addresses);
    }
}
