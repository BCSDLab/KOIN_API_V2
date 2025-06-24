package in.koreatech.koin.domain.order.address.service;

import java.util.List;

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
public class CampusAddressService {

    private final CampusDeliveryAddressRepository campusDeliveryAddressRepository;

    public CampusDeliveryAddressResponse getCampusDeliveryAddresses(CampusDeliveryAddressRequestFilter filter) {
        List<CampusDeliveryAddress> addresses = filter.getCampusDeliveryAddress(campusDeliveryAddressRepository);
        return CampusDeliveryAddressResponse.from(addresses);
    }
}
