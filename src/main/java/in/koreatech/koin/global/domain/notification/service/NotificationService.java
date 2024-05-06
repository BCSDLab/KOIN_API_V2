package in.koreatech.koin.global.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.global.domain.notification.exception.NotificationNotPermitException;
import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.repository.NotificationDetailSubscribeRepository;
import in.koreatech.koin.global.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.global.fcm.FcmClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmClient fcmClient;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationDetailSubscribeRepository notificationDetailSubscribeRepository;

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
            notification.getType()
        );
    }

    public NotificationStatusResponse checkNotification(Integer userId) {
        User user = userRepository.getById(userId);
        boolean isPermit = user.getDeviceToken() != null;
        List<NotificationSubscribe> subscribes = notificationSubscribeRepository.findAllByUserId(userId);
        List<NotificationDetailSubscribe> detailSubscribes = notificationDetailSubscribeRepository.findAllByUserId(
            userId);
        return NotificationStatusResponse.of(isPermit, subscribes, detailSubscribes);
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
            throw NotificationNotPermitException.withDetail("user.deviceToken: " + user.getDeviceToken());
        }
        if (notificationSubscribeRepository.findByUserIdAndSubscribeType(userId, subscribeType).isPresent()) {
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
        NotificationSubscribeType type = detailType.getSubscribeType();

        if (user.getDeviceToken() == null) {
            throw NotificationNotPermitException.withDetail("user.deviceToken: " + user.getDeviceToken());
        }
        if (notificationSubscribeRepository.findByUserIdAndSubscribeType(userId, type).isEmpty()) {
            throw NotificationNotPermitException.withDetail("userId: " + userId + ", type: " + type);
        }
        if (notificationDetailSubscribeRepository.findByUserIdAndDetailSubscribeType(userId, detailType.name())
            .isPresent()) {
            return;
        }

        NotificationDetailSubscribe detailSubscribe = NotificationDetailSubscribe.builder()
            .user(user)
            .subscribeType(type)
            .detailSubscribeType(detailType)
            .build();
        notificationDetailSubscribeRepository.save(detailSubscribe);
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
            throw NotificationNotPermitException.withDetail("user.deviceToken: " + user.getDeviceToken());
        }
        if (notificationSubscribeRepository.findByUserIdAndSubscribeType(userId, subscribeType).isEmpty()) {
            return;
        }
        notificationSubscribeRepository.deleteByUserIdAndSubscribeType(userId, subscribeType);
    }

    @Transactional
    public void rejectNotificationDetailSubscribe(Integer userId, NotificationDetailSubscribeType detailType) {
        User user = userRepository.getById(userId);
        NotificationSubscribeType type = detailType.getSubscribeType();

        if (user.getDeviceToken() == null) {
            throw NotificationNotPermitException.withDetail("user.deviceToken: " + user.getDeviceToken());
        }
        if (notificationSubscribeRepository.findByUserIdAndSubscribeType(userId, type).isEmpty()) {
            throw NotificationNotPermitException.withDetail("userId: " + userId + ", type: " + type);
        }
        if (notificationDetailSubscribeRepository.findByUserIdAndDetailSubscribeType(userId, detailType.name())
            .isEmpty()) {
            return;
        }
        notificationDetailSubscribeRepository.deleteByUserIdAndDetailSubscribeType(userId, detailType);
    }
}
