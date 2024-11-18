package in.koreatech.koin.domain.owner.service;

import in.koreatech.koin.domain.owner.dto.OwnerEmailVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.exception.DuplicationCompanyNumberException;
import in.koreatech.koin.domain.owner.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerEmailRequestEvent;
import in.koreatech.koin.domain.owner.model.OwnerRegisterEvent;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.model.redis.DailyVerificationLimit;
import in.koreatech.koin.domain.owner.model.redis.OwnerVerificationStatus;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.owner.repository.redis.DailyVerificationLimitRepository;
import in.koreatech.koin.domain.owner.repository.redis.OwnerVerificationStatusRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.form.OwnerRegistrationData;
import in.koreatech.koin.global.domain.email.service.MailService;
import in.koreatech.koin.global.domain.random.model.CertificateNumberGenerator;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerEmailVerificationService {

    private final JwtProvider jwtProvider;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final OwnerVerificationStatusRepository ownerVerificationStatusRepository;
    private final DailyVerificationLimitRepository dailyVerificationLimitRedisRepository;
    private final OwnerRepository ownerRepository;
    private final OwnerShopRedisRepository ownerShopRedisRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public void register(OwnerRegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw DuplicationEmailException.withDetail("email: " + request.email());
        }
        if (ownerRepository.findByCompanyRegistrationNumber(request.companyNumber()).isPresent()) {
            throw DuplicationCompanyNumberException.withDetail("companyNumber: " + request.companyNumber());
        }
        if (ownerRepository.findByAccount(request.phoneNumber()).isPresent()) {
            throw DuplicationPhoneNumberException.withDetail("account: " + request.phoneNumber());
        }
        Owner owner = request.toOwner(passwordEncoder);
        Owner saved = ownerRepository.save(owner);
        OwnerShop.OwnerShopBuilder ownerShopBuilder = OwnerShop.builder().ownerId(owner.getId());
        if (request.shopId() != null) {
            Shop shop = shopRepository.getById(request.shopId());
            ownerShopBuilder.shopId(shop.getId());
        }
        ownerShopRedisRepository.save(ownerShopBuilder.build());
        eventPublisher.publishEvent(new OwnerRegisterEvent(saved));
    }

    @Transactional
    public void requestSignUpEmailVerification(VerifyEmailRequest request) {
        userRepository.findByEmail(request.address()).ifPresent(user -> {
            throw DuplicationEmailException.withDetail("email: " + request.address());
        });
        sendCertificationEmail(request.address());
    }

    @Transactional
    public OwnerVerifyResponse verifyCodeByEmail(OwnerEmailVerifyRequest request) {
        verifyCode(request.address(), request.certificationCode());
        String token = jwtProvider.createTemporaryToken();
        return new OwnerVerifyResponse(token);
    }

    @Transactional
    public void sendResetPasswordByEmail(OwnerSendEmailRequest request) {
        User user = userRepository.getByEmail(request.address());
        sendCertificationEmail(user.getEmail());
    }

    @Transactional
    public void verifyResetPasswordCodeByEmail(OwnerPasswordResetVerifyEmailRequest request) {
        verifyCode(request.address(), request.certificationCode());
    }

    @Transactional
    public void updatePasswordByEmail(OwnerPasswordUpdateEmailRequest request) {
        User user = userRepository.getByEmail(request.address());
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

    private void sendCertificationEmail(String email) {
        setVerificationCount(email);
        String certificationCode = CertificateNumberGenerator.generate();
        mailService.sendMail(email, new OwnerRegistrationData(certificationCode));
        OwnerVerificationStatus ownerVerificationStatus = new OwnerVerificationStatus(
            email,
            certificationCode
        );
        ownerVerificationStatusRepository.save(ownerVerificationStatus);
        eventPublisher.publishEvent(new OwnerEmailRequestEvent(email));
    }

    private void verifyCode(String key, String code) {
        OwnerVerificationStatus verify = ownerVerificationStatusRepository.getByVerify(key);
        if (!Objects.equals(verify.getCertificationCode(), code)) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        ownerVerificationStatusRepository.deleteById(key);
    }
}
