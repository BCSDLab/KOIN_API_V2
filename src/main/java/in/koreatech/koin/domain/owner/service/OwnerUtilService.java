package in.koreatech.koin.domain.owner.service;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.model.dto.OwnerSmsRequestEvent;
import in.koreatech.koin.domain.owner.model.redis.DailyVerificationLimit;
import in.koreatech.koin.domain.owner.model.redis.OwnerVerificationStatus;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.owner.repository.redis.DailyVerificationLimitRepository;
import in.koreatech.koin.domain.owner.repository.redis.OwnerVerificationStatusRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.domain.random.model.CertificateNumberGenerator;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.naver.service.NaverSmsService;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerUtilService {

    private final OwnerRepository ownerRepository;
    private final UserTokenRepository userTokenRepository;
    private final ShopRepository shopRepository;

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
