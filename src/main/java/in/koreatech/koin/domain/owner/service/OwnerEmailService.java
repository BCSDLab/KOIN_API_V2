package in.koreatech.koin.domain.owner.service;

import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.email.OwnerEmailVerifyRequest;
import in.koreatech.koin.domain.owner.dto.email.OwnerPasswordResetVerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.email.OwnerPasswordUpdateEmailRequest;
import in.koreatech.koin.domain.owner.dto.email.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.email.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.email.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerEmailService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OwnerRepository ownerRepository;
    private final OwnerShopRedisRepository ownerShopRedisRepository;
    private final OwnerValidator ownerValidator;
    private final OwnerVerificationService ownerVerificationService;
    private final OwnerUtilService ownerUtilService;

    @Transactional
    public void register(OwnerRegisterRequest request) {
        ownerValidator.validateExistEmailAddress(request.email());
        ownerValidator.validateExistCompanyNumber(request.companyNumber());
        ownerValidator.validateExistPhoneNumber(request.phoneNumber());
        Owner savedOwner = ownerRepository.save(request.toOwner(passwordEncoder));
        ownerUtilService.existShopId(request.shopId());
        OwnerShop.OwnerShopBuilder ownerShopBuilder = OwnerShop.builder()
            .ownerId(savedOwner.getId())
            .shopId(request.shopId());
        ownerShopRedisRepository.save(ownerShopBuilder.build());
        ownerUtilService.sendSlackNotification(savedOwner);
    }

    @Transactional
    public void requestSignUpEmailVerification(VerifyEmailRequest request) {
        ownerValidator.validateExistEmailAddress(request.address());
        ownerVerificationService.sendCertificationEmail(request.address());
    }

    @Transactional
    public OwnerVerifyResponse verifyCodeByEmail(OwnerEmailVerifyRequest request) {
        ownerVerificationService.verifyCode(request.address(), request.certificationCode());
        String token = jwtProvider.createTemporaryToken();
        return new OwnerVerifyResponse(token);
    }

    @Transactional
    public void sendResetPasswordByEmail(OwnerSendEmailRequest request) {
        User user = userRepository.getByEmail(request.address());
        ownerVerificationService.sendCertificationEmail(user.getEmail());
    }

    @Transactional
    public void verifyResetPasswordCodeByEmail(OwnerPasswordResetVerifyEmailRequest request) {
        ownerVerificationService.verifyCode(request.address(), request.certificationCode());
    }

    @Transactional
    public void updatePasswordByEmail(OwnerPasswordUpdateEmailRequest request) {
        User user = userRepository.getByEmail(request.address());
        user.updatePassword(passwordEncoder, request.password());
    }
}
