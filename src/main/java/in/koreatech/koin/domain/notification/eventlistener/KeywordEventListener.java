package in.koreatech.koin.domain.notification.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.KoreatechArticleKeywordEvent;
import in.koreatech.koin.common.event.LostItemKeywordEvent;
import in.koreatech.koin.domain.notification.service.KeywordNotificationService;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class KeywordEventListener {

    private final KeywordNotificationService keywordNotificationService;

    @Async(value = "keywordNotificationTaskExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onKeywordRequest(KoreatechArticleKeywordEvent event) {
        keywordNotificationService.notifyArticleKeyword(event);
    }

    @Async(value = "keywordNotificationTaskExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onLostItemKeywordRequest(LostItemKeywordEvent event) {
        keywordNotificationService.notifyLostItemKeyword(event);
    }
}
