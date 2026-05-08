package in.koreatech.koin.domain.notification.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.LostItemKeywordEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LostItemKeywordEventListener {

    @Async(value = "keywordNotificationTaskExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onLostItemKeywordRequest(LostItemKeywordEvent event) {

    }
}
