package in.koreatech.koin.domain.notification.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.KoreatechArticleKeywordEvent;
import in.koreatech.koin.domain.notification.service.ArticleKeywordNotificationService;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class ArticleKeywordEventListener {

    private final ArticleKeywordNotificationService articleKeywordNotificationService;

    @Async(value = "keywordNotificationTaskExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onKeywordRequest(KoreatechArticleKeywordEvent event) {
        articleKeywordNotificationService.notifyArticleKeyword(event);
    }
}
