package in.koreatech.koin.domain.callvan.service;

import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.CALLVAN;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.callvan.model.CallvanNotification;
import in.koreatech.koin.domain.callvan.model.CallvanPushNotification;
import in.koreatech.koin.domain.callvan.model.CallvanPushNotificationFactory;
import in.koreatech.koin.domain.callvan.repository.CallvanNotificationRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanPushNotificationService {

    private final CallvanNotificationRepository callvanNotificationRepository;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final CallvanPushNotificationFactory callvanPushNotificationFactory;
    private final FcmClient fcmClient;

    public void pushNotifications(List<Integer> notificationIds) {
        if (notificationIds.isEmpty()) {
            return;
        }

        List<CallvanNotification> notifications = callvanNotificationRepository.findAllByIdInWithRelations(notificationIds);
        notifications.forEach(this::pushNotificationIfEligible);
    }

    private void pushNotificationIfEligible(CallvanNotification notification) {
        String deviceToken = notification.getRecipient().getDeviceToken();
        Integer recipientId = notification.getRecipient().getId();
        if (!StringUtils.hasText(deviceToken)
            || !notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(recipientId, CALLVAN)) {
            return;
        }

        CallvanPushNotification pushNotification = callvanPushNotificationFactory.from(notification);
        fcmClient.sendMessage(
            deviceToken,
            pushNotification.title(),
            pushNotification.message(),
            pushNotification.imageUrl(),
            pushNotification.mobileAppPath(),
            pushNotification.schemeUri(),
            pushNotification.type()
        );
    }
}
