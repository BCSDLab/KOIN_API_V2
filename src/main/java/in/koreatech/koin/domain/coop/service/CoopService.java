package in.koreatech.koin.domain.coop.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coop.dto.CoopLoginRequest;
import in.koreatech.koin.domain.coop.dto.CoopLoginResponse;
import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.model.Coop;
import in.koreatech.koin.domain.coop.model.DiningImageUploadEvent;
import in.koreatech.koin.domain.coop.model.DiningSoldOutEvent;
import in.koreatech.koin.domain.coop.repository.CoopRepository;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoopService {

    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;
    private final DiningRepository diningRepository;
    private final DiningSoldOutCacheRepository diningSoldOutCacheRepository;
    private final CoopRepository coopRepository;
    private final UserTokenRepository userTokenRepository;
    private final CoopShopService coopShopService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void changeSoldOut(SoldOutRequest soldOutRequest) {
        Dining dining = diningRepository.getById(soldOutRequest.menuId());

        if (soldOutRequest.soldOut()) {
            LocalDateTime now = LocalDateTime.now(clock);
            dining.setSoldOut(now);
            boolean isOpened = coopShopService.getIsOpened(now, CoopShopType.CAFETERIA, dining.getType(), false);
            if (isOpened && diningSoldOutCacheRepository.findById(dining.getPlace()).isEmpty()) {
                eventPublisher.publishEvent(new DiningSoldOutEvent(dining.getId(), dining.getPlace(), dining.getType()));
            }
        } else {
            dining.cancelSoldOut();
        }
    }

    @Transactional
    public void saveDiningImage(DiningImageRequest imageRequest) {
        Dining dining = diningRepository.getById(imageRequest.menuId());
        boolean isImageExist = diningRepository.existsByDateAndTypeAndImageUrlIsNotNull(dining.getDate(),
            dining.getType());

        LocalDateTime now = LocalDateTime.now(clock);
        boolean isOpened = coopShopService.getIsOpened(now, CoopShopType.CAFETERIA, dining.getType(), true);
        if (isOpened && !isImageExist) {
            eventPublisher.publishEvent(new DiningImageUploadEvent(dining.getId(), dining.getImageUrl()));
        }
        dining.setImageUrl(imageRequest.imageUrl());
    }

    @Transactional
    public CoopLoginResponse coopLogin(CoopLoginRequest request) {
        Coop coop = coopRepository.getByCoopId(request.id());
        User user = coop.getUser();

        if (!user.isSamePassword(passwordEncoder, request.password())) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        user.updateLastLoggedTime(LocalDateTime.now());

        return CoopLoginResponse.of(accessToken, savedToken.getRefreshToken());
    }
}
