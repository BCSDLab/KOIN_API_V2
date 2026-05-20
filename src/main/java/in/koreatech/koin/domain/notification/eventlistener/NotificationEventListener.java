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
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener
    public void onUserRegisterEvent(UserMarketingAgreementEvent event) {
        notificationService.permitNotificationSubscribe(event.userId(), NotificationSubscribeType.MARKETING);
    }
}
