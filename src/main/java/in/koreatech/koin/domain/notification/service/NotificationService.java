package in.koreatech.koin.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.domain.notification.exception.NotificationNotPermitException;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmClient fcmClient;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

    public void push(List<Notification> notifications) {
        for (Notification notification : notifications) {
            push(notification);
        }
    }

    public void push(Notification notification) {
        notificationRepository.save(notification);
        String deviceToken = notification.getUser().getDeviceToken();
        fcmClient.sendMessage(
            deviceToken,
            notification.getTitle(),
            notification.getMessage(),
            notification.getImageUrl(),
            notification.getMobileAppPath(),
            notification.getSchemeUri(),
            notification.getType().toLowerCase()
        );
    }

    public NotificationStatusResponse getNotificationInfo(Integer userId) {
        User user = userRepository.getById(userId);
        boolean isPermit = user.getDeviceToken() != null;
        List<NotificationSubscribe> subscribeList = notificationSubscribeRepository.findAllByUserId(userId);
        return NotificationStatusResponse.of(isPermit, subscribeList);
    }

    @Transactional
    public void permitNotification(Integer userId, String deviceToken) {
        User user = userRepository.getById(userId);
        user.permitNotification(deviceToken);
    }

    @Transactional
    public void permitNotificationSubscribe(Integer userId, NotificationSubscribeType subscribeType) {
        User user = userRepository.getById(userId);
        ensureUserDeviceToken(user.getDeviceToken());
        if (notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(userId, subscribeType)) {
            return;
        }
        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .user(user)
            .subscribeType(subscribeType)
            .build();
        notificationSubscribeRepository.save(notificationSubscribe);
    }

    @Transactional
    public void permitNotificationDetailSubscribe(Integer userId, NotificationDetailSubscribeType detailType) {
        User user = userRepository.getById(userId);
        ensureUserDeviceToken(user.getDeviceToken());

        NotificationSubscribeType type = NotificationSubscribeType.getParentType(detailType);
        if (notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailType(userId, type, detailType)) {
            return;
        }

        NotificationSubscribe detailSubscribe = NotificationSubscribe.builder()
            .user(user)
            .subscribeType(type)
            .detailType(detailType)
            .build();
        notificationSubscribeRepository.save(detailSubscribe);
    }

    @Transactional
    public void rejectNotification(Integer userId) {
        User user = userRepository.getById(userId);
        user.rejectNotification();
    }

    @Transactional
    public void rejectNotificationBySubscribeType(Integer userId, NotificationSubscribeType subscribeType) {
        User user = userRepository.getById(userId);
        ensureUserDeviceToken(user.getDeviceToken());
        notificationSubscribeRepository.deleteByUserIdAndSubscribeTypeAndDetailTypeIsNull(userId, subscribeType);
    }

    @Transactional
    public void rejectNotificationByDetailType(Integer userId, NotificationDetailSubscribeType detailType) {
        User user = userRepository.getById(userId);
        ensureUserDeviceToken(user.getDeviceToken());
        notificationSubscribeRepository.deleteByUserIdAndDetailType(userId, detailType);
    }

    private void ensureUserDeviceToken(String deviceToken) {
        if (StringUtils.isBlank(deviceToken)) {
            throw NotificationNotPermitException.withDetail("user.deviceToken: null");
        }
    }
}
