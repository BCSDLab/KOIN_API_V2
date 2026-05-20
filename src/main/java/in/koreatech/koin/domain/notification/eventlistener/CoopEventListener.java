package in.koreatech.koin.domain.notification.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.DiningImageUploadEvent;
import in.koreatech.koin.common.event.DiningSoldOutEvent;
import in.koreatech.koin.domain.notification.service.CoopNotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class CoopEventListener {

    private final CoopNotificationService coopNotificationService;

    @TransactionalEventListener
    public void onDiningSoldOutRequest(DiningSoldOutEvent event) {
        coopNotificationService.sendDiningSoldOutNotifications(event.id(), event.place(), event.diningType());
    }

    @TransactionalEventListener
    public void onDiningImageUploadRequest(DiningImageUploadEvent event) {
        coopNotificationService.sendDiningImageUploadNotifications(event.id(), event.imageUrl());
    }
}
