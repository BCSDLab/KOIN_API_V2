package in.koreatech.koin.domain.notification.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.UserMarketingAgreementEvent;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class NotificationEventListener { // TODO : 리팩터링 필요 (비즈니스로직 제거 및 알림 책임만 갖도록)

    private final NotificationService notificationService;

    @TransactionalEventListener
    public void onUserRegisterEvent(UserMarketingAgreementEvent event) {
        if (event.marketingNotificationAgreement()) {
            notificationService.permitNotificationSubscribe(event.userId(), NotificationSubscribeType.MARKETING);
        }
    }
}
