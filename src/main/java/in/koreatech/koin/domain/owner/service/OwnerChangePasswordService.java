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
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerSendPhoneRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.exception.DuplicationCertificationException;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.domain.owner.repository.EmailVerifyRequestRedisRepository;
import in.koreatech.koin.domain.owner.repository.OwnerInVerificationRedisRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.domain.email.form.OwnerPasswordChangeData;
import in.koreatech.koin.global.domain.email.service.MailService;
import in.koreatech.koin.global.domain.random.model.CertificateNumberGenerator;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.naver.service.NaverSmsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerChangePasswordService {

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
    private final NaverSmsService naverSmsService;

    public OwnerResponse getOwner(Integer ownerId) {
        Owner foundOwner = ownerRepository.getById(ownerId);
        List<Shop> shops = shopRepository.findAllByOwnerId(ownerId);
        return OwnerResponse.of(foundOwner, foundOwner.getAttachments(), shops);
    }

    public OwnerVerifyResponse verifyCode(OwnerVerifyRequest request) {
        var verify = ownerInVerificationRedisRepository.getByVerify(request.email());
        if (!Objects.equals(verify.getCertificationCode(), request.certificationCode())) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        ownerInVerificationRedisRepository.deleteById(request.email());
        String token = jwtProvider.createTemporaryToken();
        return new OwnerVerifyResponse(token);
    }

    @Transactional
    public void sendResetPasswordByEmail(OwnerSendEmailRequest request) {
        String certificationCode = CertificateNumberGenerator.generate();
        var verification = OwnerInVerification.of(request.email(), certificationCode);
        ownerInVerificationRedisRepository.save(verification);
        mailService.sendMail(request.email(), new OwnerPasswordChangeData(request.email(), certificationCode, clock));
    }

    @Transactional
    public void sendResetPasswordByPhone(OwnerSendPhoneRequest request) {
        String certificationCode = CertificateNumberGenerator.generate();
        OwnerInVerification verification = OwnerInVerification.of(request.phoneNumber(), certificationCode);
        ownerInVerificationRedisRepository.save(verification);
        naverSmsService.sendVerificationCode(certificationCode, request.phoneNumber());
    }

    @Transactional
    public void verifyResetPasswordCode(OwnerPasswordResetVerifyRequest request) {
        var verification = ownerInVerificationRedisRepository.getByVerify(request.email());
        if (!Objects.equals(verification.getCertificationCode(), request.certificationCode())) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        if (verification.isAuthed()) {
            throw new DuplicationCertificationException("이미 인증이 완료되었습니다.");
        }
        verification.verify();
        ownerInVerificationRedisRepository.save(verification);
    }

    @Transactional
    public void updatePasswordByEmail(OwnerPasswordUpdateRequest request) {
        var verification = ownerInVerificationRedisRepository.getByVerify(request.email());
        if (!verification.isAuthed()) {
            throw new KoinIllegalArgumentException("인증이 완료되지 않았습니다.");
        }
        User user = userRepository.getByEmail(request.email());
        user.updatePassword(passwordEncoder, request.password());
        userRepository.save(user);
        ownerInVerificationRedisRepository.deleteById(verification.getKey());
    }

    @Transactional
    public void updatePasswordByPhone(OwnerPasswordUpdateRequest request) {
        var verification = ownerInVerificationRedisRepository.getByVerify(request.email());
        if (!verification.isAuthed()) {
            throw new KoinIllegalArgumentException("인증이 완료되지 않았습니다.");
        }
        StringBuilder phoneNumber = new StringBuilder();
        phoneNumber.append(request.email().substring(0, 3)).append('-');
        phoneNumber.append(request.email().substring(3, 7)).append('-');
        phoneNumber.append(request.email().substring(7, 11));
        User user = userRepository.getByPhoneNumber(phoneNumber.toString());
        user.updatePassword(passwordEncoder, request.password());
        userRepository.save(user);
        ownerInVerificationRedisRepository.deleteById(verification.getKey());
    }
}
