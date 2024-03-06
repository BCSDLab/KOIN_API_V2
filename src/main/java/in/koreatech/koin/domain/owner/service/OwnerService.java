package in.koreatech.koin.domain.owner.service;

import static in.koreatech.koin.global.domain.email.model.MailForm.OWNER_REGISTRATION_MAIL_FORM;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.domain.OwnerAttachment;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.model.OwnerEmailRequestEvent;
import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.domain.owner.repository.OwnerAttachmentRepository;
import in.koreatech.koin.domain.owner.repository.OwnerInVerificationRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.model.CertificationCode;
import in.koreatech.koin.global.domain.email.model.EmailAddress;
import in.koreatech.koin.global.domain.email.service.MailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final UserRepository userRepository;
    private final OwnerInVerificationRepository ownerInVerificationRepository;
    private final MailService mailService;
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final OwnerAttachmentRepository ownerAttachmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void requestSignUpEmailVerification(VerifyEmailRequest request) {
        EmailAddress email = EmailAddress.from(request.email());
        validateEmailExist(email);

        CertificationCode certificationCode = mailService.sendMail(email, OWNER_REGISTRATION_MAIL_FORM);

        OwnerInVerification ownerInVerification = OwnerInVerification.of(email.email(),
            certificationCode.getValue());
        ownerInVerificationRepository.save(ownerInVerification);

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
        List<OwnerAttachment> attachments = ownerAttachmentRepository.findAllByOwnerId(ownerId);
        List<Shop> shops = shopRepository.findAllByOwnerId(ownerId);
        return OwnerResponse.of(foundOwner, attachments, shops);
    }
}
