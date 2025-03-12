package in.koreatech.koin.domain.owner.service;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.dto.OwnerRegisterEvent;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserTokenRedisRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerUtilService {

    private final OwnerRepository ownerRepository;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void sendSlackNotification(Owner owner) {
        eventPublisher.publishEvent(new OwnerRegisterEvent(
                owner.getUser().getName(),
                owner.getId()
        ));
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
