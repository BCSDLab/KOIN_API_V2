package in.koreatech.koin.domain.address.client;

import in.koreatech.koin.domain.order.address.dto.RoadNameAddressApiResponse;

public interface AddressClient {

    RoadNameAddressApiResponse searchAddress(String keyword, int currentPage, int countPerPage);
}
