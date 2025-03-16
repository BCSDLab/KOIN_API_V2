package in.koreatech.koin.domain.owner.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import in.koreatech.koin._common.event.OwnerRegisterEvent;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerUtilService {

    private final OwnerRepository ownerRepository;
    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void sendSlackNotification(Owner owner) {
        eventPublisher.publishEvent(new OwnerRegisterEvent(
            owner.getUser().getName(),
            owner.getId()
        ));
    }

    public void validateExistShopId(Integer shopId) {
        if (shopId != null && !shopRepository.existsById(shopId)) {
            throw ShopNotFoundException.withDetail("shopId: " + shopId);
        }
    }

    public User extractUserByAccount(String account) {
        Owner owner = ownerRepository.getByAccount(account);
        return owner.getUser();
    }
}
