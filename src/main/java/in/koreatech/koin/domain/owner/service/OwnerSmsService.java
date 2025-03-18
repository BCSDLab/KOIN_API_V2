package in.koreatech.koin.domain.owner.service;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.sms.OwnerAccountCheckExistsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerLoginRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerLoginResponse;
import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordResetVerifySmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordUpdateSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerRegisterByPhoneRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSendSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSmsVerifyRequest;
import in.koreatech.koin.domain.owner.dto.sms.VerifySmsRequest;
import in.koreatech.koin.domain.owner.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.owner.repository.redis.OwnerVerificationStatusRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerSmsService {

    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final OwnerShopRedisRepository ownerShopRedisRepository;
    private final OwnerVerificationStatusRepository ownerInVerificationRedisRepository;
    private final OwnerValidator ownerValidator;
    private final OwnerUtilService ownerUtilService;
    private final OwnerVerificationService ownerVerificationService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public OwnerLoginResponse ownerLogin(OwnerLoginRequest request, UserAgentInfo userAgentInfo) {
        User user = ownerUtilService.extractUserByAccount(request.account());
        ownerValidator.validatePassword(user, request.password());
        ownerValidator.validateAuth(user);
        String accessToken = jwtProvider.createToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId(), userAgentInfo.getType());
        user.updateLastLoggedTime(LocalDateTime.now());
        return OwnerLoginResponse.of(accessToken, refreshToken);
    }

    @Transactional
    public void registerByPhone(OwnerRegisterByPhoneRequest request) {
        ownerValidator.validateExistPhoneNumber(request.phoneNumber());
        ownerValidator.validateExistCompanyNumber(request.companyNumber());
        Owner savedOwner = ownerRepository.save(request.toOwner(passwordEncoder));

        ownerUtilService.validateExistShopId(request.shopId());
        OwnerShop ownerShop = OwnerShop.builder()
            .ownerId(savedOwner.getId())
            .shopId(request.shopId())
            .shopName(request.shopName())
            .shopNumber(request.shopNumber())
            .build();
        ownerShopRedisRepository.save(ownerShop);
        ownerInVerificationRedisRepository.deleteByVerify(savedOwner.getUser().getPhoneNumber());
        ownerUtilService.sendSlackNotification(savedOwner);
    }

    @Transactional
    public void requestSignUpSmsVerification(VerifySmsRequest request) {
        ownerValidator.validateExistPhoneNumber(request.phoneNumber());
        ownerVerificationService.sendCertificationSms(request.phoneNumber());
    }

    @Transactional
    public OwnerVerifyResponse verifyCodeBySms(OwnerSmsVerifyRequest request) {
        ownerVerificationService.verifyCode(request.phoneNumber(), request.certificationCode());
        return new OwnerVerifyResponse(jwtProvider.createTemporaryToken());
    }

    @Transactional
    public void sendResetPasswordBySms(OwnerSendSmsRequest request) {
        User user = userRepository.getByPhoneNumber(request.phoneNumber(), OWNER);
        ownerVerificationService.sendCertificationSms(user.getPhoneNumber());
    }

    @Transactional
    public void verifyResetPasswordCodeBySms(OwnerPasswordResetVerifySmsRequest request) {
        ownerVerificationService.verifyCode(request.phoneNumber(), request.certificationCode());
    }

    @Transactional
    public void updatePasswordBySms(OwnerPasswordUpdateSmsRequest request) {
        User user = userRepository.getByPhoneNumber(request.phoneNumber(), OWNER);
        user.updatePassword(passwordEncoder, request.password());
    }

    public void checkExistsAccount(OwnerAccountCheckExistsRequest request) {
        ownerRepository.findByAccount(request.account()).ifPresent(user -> {
            throw DuplicationPhoneNumberException.withDetail("account: " + request.account());
        });
    }
}
