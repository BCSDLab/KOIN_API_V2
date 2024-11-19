package in.koreatech.koin.domain.owner.service;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.model.dto.OwnerRegisterEvent;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerUtilService {

    private final OwnerRepository ownerRepository;
    private final UserTokenRepository userTokenRepository;
    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void sendSlackNotification(Owner owner) {
        eventPublisher.publishEvent(new OwnerRegisterEvent(
                owner.getUser().getName(),
                owner.getUser().getEmail(),
                owner.getId()
        ));
    }

    public String saveRefreshToken(User user) {
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        return savedToken.getRefreshToken();
    }

    public void setShopId(Integer shopId, OwnerShop.OwnerShopBuilder builder) {
        if (shopId != null) {
            Shop shop = shopRepository.getById(shopId);
            builder.shopId(shop.getId());
        }
    }

    public User extractUserByAccount(String account) {
        Owner owner = ownerRepository.getByAccount(account);
        return owner.getUser();
    }
}
