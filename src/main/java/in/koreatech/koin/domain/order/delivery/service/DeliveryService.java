package in.koreatech.koin.domain.order.delivery.service;

import static in.koreatech.koin.global.cache.CacheKey.RIDER_MESSAGES_CACHE;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;
import in.koreatech.koin.domain.order.address.repository.CampusDeliveryAddressRepository;
import in.koreatech.koin.domain.order.delivery.dto.RiderMessageResponse;
import in.koreatech.koin.domain.order.delivery.dto.UserCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.delivery.dto.UserDeliveryAddressResponse;
import in.koreatech.koin.domain.order.delivery.dto.UserOffCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.delivery.model.OffCampusDeliveryAddress;
import in.koreatech.koin.domain.order.delivery.model.UserDeliveryAddress;
import in.koreatech.koin.domain.order.delivery.repository.RiderMessageRepository;
import in.koreatech.koin.domain.order.delivery.repository.UserDeliveryAddressRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryAddressValidator deliveryAddressValidator;
    private final UserRepository userRepository;
    private final UserDeliveryAddressRepository deliveryAddressRepository;
    private final CampusDeliveryAddressRepository campusDeliveryAddressRepository;
    private final RiderMessageRepository riderMessageRepository;

    @Transactional
    public UserDeliveryAddressResponse addOffCampusDeliveryAddress(UserOffCampusDeliveryAddressRequest request,
        Integer userId) {
        User user = userRepository.getById(userId);
        OffCampusDeliveryAddress offCampusDeliveryAddress = request.toOffCampusAddress();

        validateOffCampusAddress(offCampusDeliveryAddress);

        UserDeliveryAddress userDeliveryAddress = deliveryAddressRepository.save(
            UserDeliveryAddress.ofOffCampus(user, offCampusDeliveryAddress)
        );

        return UserDeliveryAddressResponse.of(userDeliveryAddress.getId(), userDeliveryAddress.getFullDeliveryAddress());
    }

    @Transactional
    public UserDeliveryAddressResponse addCampusDeliveryAddress(UserCampusDeliveryAddressRequest request,
        Integer userId) {
        User user = userRepository.getById(userId);

        CampusDeliveryAddress campusDeliveryAddress = campusDeliveryAddressRepository.getById(
            request.campusDeliveryAddressId());

        UserDeliveryAddress userDeliveryAddress = deliveryAddressRepository.save(
            UserDeliveryAddress.ofCampus(user, campusDeliveryAddress)
        );

        return UserDeliveryAddressResponse.of(userDeliveryAddress.getId(), userDeliveryAddress.getFullDeliveryAddress());
    }

    public void validateOffCampusAddress(OffCampusDeliveryAddress offCampusDeliveryAddress) {
        deliveryAddressValidator.validateOffCampusAddress(offCampusDeliveryAddress);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RIDER_MESSAGES_CACHE)
    public RiderMessageResponse getRiderMessage() {
        return RiderMessageResponse.from(riderMessageRepository.findAll());
    }
}
