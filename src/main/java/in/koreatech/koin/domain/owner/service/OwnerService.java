package in.koreatech.koin.domain.owner.service;

import static in.koreatech.koin.global.common.email.model.MailForm.OWNER_REGISTRATION_MAIL_FORM;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.domain.OwnerAttachment;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.domain.owner.repository.OwnerAttachmentRepository;
import in.koreatech.koin.domain.owner.repository.OwnerInVerificationRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.common.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.common.email.model.CertificationCode;
import in.koreatech.koin.global.common.email.model.Email;
import in.koreatech.koin.global.common.email.service.MailService;
import in.koreatech.koin.global.common.slack.service.SlackService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final UserRepository userRepository;
    private final OwnerInVerificationRepository ownerInVerificationRepository;
    private final MailService mailService;
    private final SlackService slackService;
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final OwnerAttachmentRepository ownerAttachmentRepository;

    public void requestSignUpEmailVerification(VerifyEmailRequest request) {
        Email email = Email.from(request.email());
        validateEmailExist(email);

        email.validateSendable();
        CertificationCode certificationCode = mailService.sendMail(email, OWNER_REGISTRATION_MAIL_FORM);

        OwnerInVerification ownerInVerification = OwnerInVerification.from(email.getEmail(),
            certificationCode.getValue());
        ownerInVerificationRepository.save(ownerInVerification);

        slackService.noticeEmailVerification(ownerInVerification);
    }

    private void validateEmailExist(Email email) {
        userRepository.findByEmail(email.getEmail())
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("email: " + email.getEmail());
            });
    }

    public OwnerResponse getOwner(Long ownerId) {
        Owner foundOwner = ownerRepository.getById(ownerId);
        List<OwnerAttachment> attachments = ownerAttachmentRepository.findAllByOwnerId(ownerId);
        List<Shop> shops = shopRepository.findAllByOwnerId(ownerId);
        return OwnerResponse.of(foundOwner, attachments, shops);
    }
}
