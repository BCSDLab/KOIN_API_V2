package in.koreatech.koin.domain.owner.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.dto.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.exception.DuplicationCompanyNumberException;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerEmailRequestEvent;
import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.domain.owner.model.OwnerRegisterEvent;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.repository.OwnerInVerificationRedisRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.model.CertificationCode;
import in.koreatech.koin.global.domain.email.model.EmailAddress;
import static in.koreatech.koin.global.domain.email.model.MailForm.OWNER_REGISTRATION_MAIL_FORM;
import in.koreatech.koin.global.domain.email.service.MailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final MailService mailService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final OwnerShopRedisRepository ownerShopRedisRepository;
    private final OwnerInVerificationRedisRepository ownerInVerificationRedisRepository;

    @Transactional
    public void requestSignUpEmailVerification(VerifyEmailRequest request) {
        EmailAddress email = EmailAddress.from(request.email());
        validateEmailExist(email);
        CertificationCode certificationCode = mailService.sendMail(email, OWNER_REGISTRATION_MAIL_FORM);
        OwnerInVerification ownerInVerification = OwnerInVerification.of(
            email.email(),
            certificationCode.getValue()
        );
        ownerInVerificationRedisRepository.save(ownerInVerification);
        eventPublisher.publishEvent(new OwnerEmailRequestEvent(ownerInVerification.getEmail()));
    }

    private void validateEmailExist(EmailAddress email) {
        userRepository.findByEmail(email.email())
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("email: " + email.email());
            });
    }

    public OwnerResponse getOwner(Long ownerId) {
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
        var shop = shopRepository.findById(request.shopId())
            .orElse(null);
        ownerShopRedisRepository.save(OwnerShop.builder()
            .ownerId(owner.getId())
            .shopId(shop == null ? null : shop.getId())
            .build());
        ownerShopRedisRepository.save(OwnerShop.builder()
            .build());
        eventPublisher.publishEvent(new OwnerRegisterEvent(saved));
    }
}
