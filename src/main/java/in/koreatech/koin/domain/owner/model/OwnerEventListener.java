package in.koreatech.koin.domain.owner.model;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.global.domain.slack.service.SlackService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW) // 무슨 일이 일어날까
public class OwnerEventListener {

    private final SlackService slackService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerEmailRequest(OwnerEmailRequestEvent event) {
        slackService.noticeEmailVerification(event.email());
    }
}
