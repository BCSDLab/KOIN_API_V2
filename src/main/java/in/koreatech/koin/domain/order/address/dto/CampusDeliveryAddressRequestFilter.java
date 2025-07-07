package in.koreatech.koin.domain.order.address.dto;

import java.util.List;
import java.util.function.Function;

import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;
import in.koreatech.koin.domain.order.address.repository.CampusDeliveryAddressRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CampusDeliveryAddressRequestFilter {

    ALL("ALL", 0, repository -> repository.findAll()),
    DORMITORY("DORMITORY", 1, repository -> repository.findByCampusAddressType_Id(1)),
    COLLEGE_BUILDING("COLLEGE_BUILDING", 2, repository -> repository.findByCampusAddressType_Id(2)),
    ETC("ETC", 3, repository -> repository.findByCampusAddressType_Id(3));

    private final String value;
    private final Integer id;
    private final Function<CampusDeliveryAddressRepository, List<CampusDeliveryAddress>> queryFunction;

    public List<CampusDeliveryAddress> getCampusDeliveryAddress(CampusDeliveryAddressRepository repository) {
        return this.queryFunction.apply(repository);
    }
}
