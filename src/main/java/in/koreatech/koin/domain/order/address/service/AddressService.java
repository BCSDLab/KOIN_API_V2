package in.koreatech.koin.domain.order.address.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.address.dto.UserCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.address.dto.UserDeliveryAddressResponse;
import in.koreatech.koin.domain.order.address.dto.UserOffCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;
import in.koreatech.koin.domain.order.address.model.OffCampusDeliveryAddress;
import in.koreatech.koin.domain.order.address.model.UserDeliveryAddress;
import in.koreatech.koin.domain.order.address.repository.CampusDeliveryAddressRepository;
import in.koreatech.koin.domain.order.address.repository.UserDeliveryAddressRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final OffCampusAddressValidator offCampusAddressValidator;
    private final UserRepository userRepository;
    private final UserDeliveryAddressRepository deliveryAddressRepository;
    private final CampusDeliveryAddressRepository campusDeliveryAddressRepository;

    @Transactional
    public UserDeliveryAddressResponse addOffCampusDeliveryAddress(UserOffCampusDeliveryAddressRequest request,
        Integer userId) {
        User user = userRepository.getById(userId);

        OffCampusDeliveryAddress offCampusDeliveryAddress = request.toOffCampusAddress();
        offCampusAddressValidator.validateAddress(offCampusDeliveryAddress);

        UserDeliveryAddress userDeliveryAddress = deliveryAddressRepository.save(
            UserDeliveryAddress.ofOffCampus(user, offCampusDeliveryAddress, request.toRider())
        );

        return UserDeliveryAddressResponse.of(userDeliveryAddress.getId(), userDeliveryAddress.getFullDeliveryAddress(),
            userDeliveryAddress.getToRider());
    }

    @Transactional
    public UserDeliveryAddressResponse addCampusDeliveryAddress(UserCampusDeliveryAddressRequest request,
        Integer userId) {
        User user = userRepository.getById(userId);

        CampusDeliveryAddress campusDeliveryAddress = campusDeliveryAddressRepository.getById(
            request.campusDeliveryAddressId());

        UserDeliveryAddress userDeliveryAddress = deliveryAddressRepository.save(
            UserDeliveryAddress.ofCampus(user, campusDeliveryAddress, request.toRider())
        );

        return UserDeliveryAddressResponse.of(userDeliveryAddress.getId(), userDeliveryAddress.getFullDeliveryAddress(),
            userDeliveryAddress.getToRider());
    }
}
