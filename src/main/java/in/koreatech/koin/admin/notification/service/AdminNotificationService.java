package in.koreatech.koin.admin.notification.service;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_DETAIL_SUBSCRIBE_TYPE;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.admin.manager.repository.AdminRepository;
import in.koreatech.koin.admin.notification.dto.AdminNotificationRequest;
import in.koreatech.koin.admin.notification.repository.AdminNotificationRepository;
import in.koreatech.koin.admin.notification.repository.AdminNotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationService {

    private final AdminNotificationSubscribeRepository adminNotificationSubscribeRepository;
    private final AdminNotificationRepository adminNotificationRepository;
    private final AdminRepository adminRepository;
    private final FcmClient fcmClient;

    public void sendNotification(AdminNotificationRequest request, Integer adminId) {
        Admin admin = adminRepository.getById(adminId);
        validateDetailTypeMatchesSubscribeType(request.subscribeType(), request.detailSubscribeType());
        List<NotificationSubscribe> notificationSubscribes = getNotificationSubscribes(
            request.subscribeType(), request.detailSubscribeType()
        );

        for (NotificationSubscribe notificationSubscribe : notificationSubscribes) {
            try {
                User user = notificationSubscribe.getUser();
                Notification notification = Notification.of(
                    request.mobileAppPath(),
                    request.schemaUrl(),
                    request.title(),
                    request.message(),
                    request.imageUrl(),
                    user
                );
                saveAndSendNotification(notification);
            } catch (Exception e) {
                log.warn("FCM 알림 전송 과정에서 에러 발생", e);
            }
        }
    }

    @Transactional
    public void saveAndSendNotification(Notification notification) {
        adminNotificationRepository.save(notification);
        fcmClient.sendMessage(
            notification.getUser().getDeviceToken(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getImageUrl(),
            notification.getMobileAppPath(),
            notification.getSchemeUri(),
            notification.getType().toLowerCase()
        );
    }

    private void validateDetailTypeMatchesSubscribeType(
        NotificationSubscribeType subscribeType, NotificationDetailSubscribeType detailSubscribeType
    ) {
        if (detailSubscribeType == null) {
            return;
        }

        if (subscribeType.isNotContainsDetailType(detailSubscribeType)) {
            throw CustomException.of(
                INVALID_DETAIL_SUBSCRIBE_TYPE,
                String.format("subscribeType: %s, detailSubscribeType: %s", subscribeType, detailSubscribeType)
            );
        }
    }

    private List<NotificationSubscribe> getNotificationSubscribes(
        NotificationSubscribeType subscribeType, NotificationDetailSubscribeType detailSubscribeType
    ) {
        if (detailSubscribeType == null) {
            return adminNotificationSubscribeRepository.findAllBySubscribeType(subscribeType);
        }
        return adminNotificationSubscribeRepository.findAllBySubscribeTypeAndDetailType(
            subscribeType, detailSubscribeType
        );
    }
}
