package in.koreatech.koin.domain.owner.service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.exception.DuplicationCertificationException;
import in.koreatech.koin.domain.owner.exception.DuplicationCompanyNumberException;
import in.koreatech.koin.domain.owner.model.EmailVerifyRequest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerEmailRequestEvent;
import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.domain.owner.model.OwnerRegisterEvent;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.repository.EmailVerifyRequestRedisRepository;
import in.koreatech.koin.domain.owner.repository.OwnerInVerificationRedisRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.form.OwnerPasswordChangeData;
import in.koreatech.koin.global.domain.email.form.OwnerRegistrationData;
import in.koreatech.koin.global.domain.email.service.MailService;
import in.koreatech.koin.global.domain.random.model.CertificateNumberGenerator;
import in.koreatech.koin.global.exception.RequestTooFastException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final JwtProvider jwtProvider;
    private final Clock clock;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final OwnerShopRedisRepository ownerShopRedisRepository;
    private final OwnerInVerificationRedisRepository ownerInVerificationRedisRepository;
    private final EmailVerifyRequestRedisRepository emailVerifyRequestRedisRepository;

    @Transactional
    public void requestSignUpEmailVerification(VerifyEmailRequest request) {
        emailVerifyRequestRedisRepository.findById(request.email()).ifPresent(it -> {
            throw new RequestTooFastException("요청이 너무 빠릅니다. %d초 뒤에 다시 시도해주세요".formatted(it.getExpiration()));
        });
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw DuplicationEmailException.withDetail("email: " + request.email());
        });
        String certificationCode = CertificateNumberGenerator.generate();
        mailService.sendMail(request.email(), new OwnerRegistrationData(certificationCode));
        OwnerInVerification ownerInVerification = OwnerInVerification.of(
            request.email(),
            certificationCode
        );
        ownerInVerificationRedisRepository.save(ownerInVerification);
        emailVerifyRequestRedisRepository.save(new EmailVerifyRequest(request.email()));
        eventPublisher.publishEvent(new OwnerEmailRequestEvent(ownerInVerification.getEmail()));
    }

    public OwnerResponse getOwner(Integer ownerId) {
        Owner foundOwner = ownerRepository.getById(ownerId);
        List<Shop> shops = shopRepository.findAllByOwnerId(ownerId);
        return OwnerResponse.of(foundOwner, foundOwner.getAttachments(), shops);
    }

    @Transactional
    public void register(OwnerRegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw DuplicationEmailException.withDetail("email: " + request.email());
        }
        if (ownerRepository.findByCompanyRegistrationNumber(request.companyNumber()).isPresent()) {
            throw DuplicationCompanyNumberException.withDetail("companyNumber: " + request.companyNumber());
        }
        Owner owner = request.toOwner(passwordEncoder);
        Owner saved = ownerRepository.save(owner);
        if (request.shopId() != null) {
            var shop = shopRepository.getById(request.shopId());
            ownerShopRedisRepository.save(OwnerShop.builder()
                .ownerId(owner.getId())
                .shopId(shop.getId())
                .build());
            ownerShopRedisRepository.save(OwnerShop.builder()
                .build());
        } else {
            ownerShopRedisRepository.save(OwnerShop.builder()
                .ownerId(owner.getId())
                .build());
            ownerShopRedisRepository.save(OwnerShop.builder()
                .build());
        }

        eventPublisher.publishEvent(new OwnerRegisterEvent(saved));
    }

    public OwnerVerifyResponse verifyCode(OwnerVerifyRequest request) {
        var verify = ownerInVerificationRedisRepository.getByEmail(request.email());
        if (!Objects.equals(verify.getCertificationCode(), request.certificationCode())) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        ownerInVerificationRedisRepository.deleteById(request.email());
        String token = jwtProvider.createTemporaryToken();
        return new OwnerVerifyResponse(token);
    }

    @Transactional
    public void sendResetPasswordEmail(OwnerSendEmailRequest request) {
        String certificationCode = CertificateNumberGenerator.generate();
        var verification = OwnerInVerification.of(request.email(), certificationCode);
        ownerInVerificationRedisRepository.save(verification);
        mailService.sendMail(request.email(), new OwnerPasswordChangeData(request.email(), certificationCode, clock));
    }

    @Transactional
    public void verifyResetPasswordCode(OwnerPasswordResetVerifyRequest request) {
        var verification = ownerInVerificationRedisRepository.getByEmail(request.email());
        if (!Objects.equals(verification.getCertificationCode(), request.certificationCode())) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        if (verification.isAuthed()) {
            throw new DuplicationCertificationException("이미 인증이 완료되었습니다.");
        }
        verification.verify();
        ownerInVerificationRedisRepository.save(verification);
    }

    @Transactional
    public void updatePassword(OwnerPasswordUpdateRequest request) {
        var verification = ownerInVerificationRedisRepository.getByEmail(request.email());
        if (!verification.isAuthed()) {
            throw new IllegalArgumentException("인증이 완료되지 않았습니다.");
        }
        User user = userRepository.getByEmail(request.email());
        user.updatePassword(passwordEncoder, request.password());
        userRepository.save(user);
        ownerInVerificationRedisRepository.deleteById(verification.getEmail());
    }
}
