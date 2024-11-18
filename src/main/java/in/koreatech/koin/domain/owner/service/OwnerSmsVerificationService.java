package in.koreatech.koin.domain.owner.service;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import in.koreatech.koin.domain.owner.dto.sms.OwnerAccountCheckExistsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerLoginRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerLoginResponse;
import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordResetVerifySmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordUpdateSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerRegisterByPhoneRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSendSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSmsVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.sms.VerifySmsRequest;
import in.koreatech.koin.domain.owner.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.dto.OwnerRegisterBySmsEvent;
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
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerSmsVerificationService {

    private final JwtProvider jwtProvider;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final OwnerVerificationStatusRepository ownerVerificationStatusRepository;
    private final DailyVerificationLimitRepository dailyVerificationLimitRedisRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final ShopRepository shopRepository;
    private final OwnerShopRedisRepository ownerShopRedisRepository;
    private final OwnerValidator ownerValidator;
    private final NaverSmsService naverSmsService;

    @Transactional
    public OwnerLoginResponse ownerLogin(OwnerLoginRequest request) {
        User user = extractUserByAccount(request.account());
        ownerValidator.validatePassword(user, request.password());
        ownerValidator.validateAuth(user);
        String accessToken = jwtProvider.createToken(user);
        String refreshToken = saveRefreshToken(user);
        user.updateLastLoggedTime(LocalDateTime.now());
        return OwnerLoginResponse.of(accessToken, refreshToken);
    }

    @Transactional
    public void registerByPhone(OwnerRegisterByPhoneRequest request) {
        ownerValidator.validateExistPhoneNumber(request.phoneNumber());
        ownerValidator.validateExistCompanyNumber(request.companyNumber());
        Owner owner = request.toOwner(passwordEncoder);
        Owner saved = ownerRepository.save(owner);
        OwnerShop.OwnerShopBuilder ownerShopBuilder = OwnerShop.builder().ownerId(owner.getId());
        setShopId(request.shopId(), ownerShopBuilder);
        ownerShopRedisRepository.save(ownerShopBuilder.build());
        eventPublisher.publishEvent(new OwnerRegisterBySmsEvent(saved));
    }

    @Transactional
    public void requestSignUpSmsVerification(VerifySmsRequest request) {
        ownerValidator.validateExistPhoneNumber(request.phoneNumber());
        sendCertificationSms(request.phoneNumber());
    }

    @Transactional
    public OwnerVerifyResponse verifyCodeBySms(OwnerSmsVerifyRequest request) {
        verifyCode(request.phoneNumber(), request.certificationCode());
        return new OwnerVerifyResponse(jwtProvider.createTemporaryToken());
    }

    @Transactional
    public void sendResetPasswordBySms(OwnerSendSmsRequest request) {
        User user = userRepository.getByPhoneNumber(request.phoneNumber(), OWNER);
        sendCertificationSms(user.getPhoneNumber());
    }

    @Transactional
    public void verifyResetPasswordCodeBySms(OwnerPasswordResetVerifySmsRequest request) {
        verifyCode(request.phoneNumber(), request.certificationCode());
    }

    @Transactional
    public void updatePasswordBySms(OwnerPasswordUpdateSmsRequest request) {
        User user = userRepository.getByPhoneNumber(request.phoneNumber(), OWNER);
        user.updatePassword(passwordEncoder, request.password());
    }

    private void setVerificationCount(String key) {
        Optional<DailyVerificationLimit> dailyVerificationLimit = dailyVerificationLimitRedisRepository.findById(key);
        if (dailyVerificationLimit.isEmpty()) {
            dailyVerificationLimitRedisRepository.save(new DailyVerificationLimit(key));
        } else {
            DailyVerificationLimit dailyVerification = dailyVerificationLimit.get();
            dailyVerification.requestVerification();
            dailyVerificationLimitRedisRepository.save(dailyVerification);
        }
    }

    private void sendCertificationSms(String phoneNumber) {
        setVerificationCount(phoneNumber);
        String certificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(certificationCode, phoneNumber);
        OwnerVerificationStatus ownerVerificationStatus = new OwnerVerificationStatus(
            phoneNumber,
            certificationCode
        );
        ownerVerificationStatusRepository.save(ownerVerificationStatus);
        eventPublisher.publishEvent(new OwnerSmsRequestEvent(phoneNumber));
    }

    private void verifyCode(String key, String code) {
        OwnerVerificationStatus verify = ownerVerificationStatusRepository.getByVerify(key);
        if (!Objects.equals(verify.getCertificationCode(), code)) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        ownerVerificationStatusRepository.deleteById(key);
    }

    public void checkExistsAccount(OwnerAccountCheckExistsRequest request) {
        ownerRepository.findByAccount(request.account()).ifPresent(user -> {
            throw DuplicationPhoneNumberException.withDetail("account: " + request.account());
        });
    }

    private String saveRefreshToken(User user) {
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        return savedToken.getRefreshToken();
    }

    private void setShopId(Integer shopId, OwnerShop.OwnerShopBuilder builder) {
        if (shopId != null) {
            Shop shop = shopRepository.getById(shopId);
            builder.shopId(shop.getId());
        }
    }

    private User extractUserByAccount(String account) {
        Owner owner = ownerRepository.getByAccount(account);
        return owner.getUser();
    }
}
