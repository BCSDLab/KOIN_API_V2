package in.koreatech.koin.infrastructure.email.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.BEFORE_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.StudentRegisterRequestEvent;
import in.koreatech.koin._common.event.StudentFindPasswordEvent;
import in.koreatech.koin._common.event.UserEmailVerificationSendEvent;
import in.koreatech.koin.infrastructure.email.form.StudentFindPasswordEmailForm;
import in.koreatech.koin.infrastructure.email.form.StudentRegisterRequestEmailForm;
import in.koreatech.koin.infrastructure.email.service.EmailService;
import in.koreatech.koin.infrastructure.email.form.EmailForm;
import in.koreatech.koin.infrastructure.email.form.UserVerificationEmailForm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = BEFORE_COMMIT)
    public void onUserEmailVerificationSendEvent(UserEmailVerificationSendEvent event) {
        EmailForm emailForm = new UserVerificationEmailForm(event.verificationCode());
        emailService.sendVerificationEmail(event.email(), emailForm);
    }

    @TransactionalEventListener(phase = BEFORE_COMMIT)
    public void onStudentEmailRequestEvent(StudentRegisterRequestEvent event) {
        EmailForm emailForm = new StudentRegisterRequestEmailForm(event.serverUrl(), event.authToken());
        emailService.sendVerificationEmail(event.email(), emailForm);
    }

    @TransactionalEventListener(phase = BEFORE_COMMIT)
    public void onStudentFindPasswordEvent(StudentFindPasswordEvent event) {
        EmailForm emailForm = new StudentFindPasswordEmailForm(event.serverUrl(), event.resetToken());
        emailService.sendVerificationEmail(event.email(), emailForm);
    }
}
