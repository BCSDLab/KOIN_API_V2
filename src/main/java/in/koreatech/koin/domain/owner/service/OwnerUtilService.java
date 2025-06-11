package in.koreatech.koin.domain.owner.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import in.koreatech.koin._common.event.OwnerRegisterEvent;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerUtilService {

    private final OwnerRepository ownerRepository;
    private final OwnerShopRedisRepository ownerShopRedisRepository;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void sendSlackNotification(Owner owner) {
        String ownerName = owner.getUser().getName();
        String shopName = ownerShopRedisRepository.findById(owner.getId()).getShopName();
        OwnerRegisterEvent ownerRegisterEvent = new OwnerRegisterEvent(ownerName, shopName);
        eventPublisher.publishEvent(ownerRegisterEvent);
    }

    public String saveRefreshToken(User user) {
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRedisRepository.save(UserToken.create(user.getId(), refreshToken));
        return savedToken.getRefreshToken();
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
