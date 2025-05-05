package in.koreatech.koin.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.notification.exception.NotificationNotPermitException;
import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
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

    public NotificationStatusResponse checkNotification(Integer userId) {
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
        if (user.getDeviceToken() == null) {
            throw NotificationNotPermitException.withDetail("user.deviceToken: null");
        }
        if (notificationSubscribeRepository
            .findByUserIdAndSubscribeTypeAndDetailType(userId, subscribeType, null).isPresent()) {
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
        NotificationSubscribeType type = NotificationSubscribeType.getParentType(detailType);

        if (user.getDeviceToken() == null) {
            throw NotificationNotPermitException.withDetail("user.deviceToken: null");
        }
        if (notificationSubscribeRepository
            .findByUserIdAndSubscribeTypeAndDetailType(userId, type, null).isEmpty()) {
            throw NotificationNotPermitException.withDetail("userId: " + userId + ", type: " + type);
        }
        if (notificationSubscribeRepository
            .findByUserIdAndSubscribeTypeAndDetailType(userId, type, detailType).isPresent()) {
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
    public void rejectNotificationByType(Integer userId, NotificationSubscribeType subscribeType) {
        User user = userRepository.getById(userId);
        if (user.getDeviceToken() == null) {
            throw NotificationNotPermitException.withDetail("user.deviceToken: null");
        }
        if (notificationSubscribeRepository
            .findByUserIdAndSubscribeTypeAndDetailType(userId, subscribeType, null).isEmpty()) {
            return;
        }
        notificationSubscribeRepository.deleteByUserIdAndSubscribeTypeAndDetailType(userId, subscribeType, null);
    }

    @Transactional
    public void rejectNotificationDetailSubscribe(Integer userId, NotificationDetailSubscribeType detailType) {
        User user = userRepository.getById(userId);
        NotificationSubscribeType type = NotificationSubscribeType.getParentType(detailType);

        if (user.getDeviceToken() == null) {
            throw NotificationNotPermitException.withDetail("user.deviceToken: null");
        }
        if (notificationSubscribeRepository
            .findByUserIdAndSubscribeTypeAndDetailType(userId, type, null).isEmpty()) {
            throw NotificationNotPermitException.withDetail("userId: " + userId + ", type: " + type);
        }
        if (notificationSubscribeRepository
            .findByUserIdAndSubscribeTypeAndDetailType(userId, type, detailType).isEmpty()) {
            return;
        }
        notificationSubscribeRepository.deleteByUserIdAndSubscribeTypeAndDetailType(userId, type, detailType);
    }
}
