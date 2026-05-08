package in.koreatech.koin.domain.notification.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.LostItemKeywordEvent;
import in.koreatech.koin.domain.notification.service.LostItemKeywordNotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LostItemKeywordEventListener {

    private final LostItemKeywordNotificationService lostItemKeywordNotificationService;

    @Async(value = "keywordNotificationTaskExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onLostItemKeywordRequest(LostItemKeywordEvent event) {
        lostItemKeywordNotificationService.notifyLostItemKeyword(event);
    }
}
