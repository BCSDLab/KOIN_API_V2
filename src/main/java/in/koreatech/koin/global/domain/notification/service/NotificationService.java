package in.koreatech.koin.global.domain.notification.service;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static in.koreatech.koin.global.fcm.MobileAppPath.HOME;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coop.model.DiningSoldOutEvent;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.global.domain.notification.dto.NotificationSubscribePermitRequest;
import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
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
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

    public void pushSoldOutNotification(DiningSoldOutEvent event) {
        Notification notification = notificationFactory.generateSoldOutNotification(HOME, event.place());
        notificationRepository.save(notification);
        List<String> deviceTokenList = generateDeviceTokenListBySubscribeSoldOut();
        pushAll(notification, deviceTokenList);
    }

    private List<String> generateDeviceTokenListBySubscribeSoldOut() {
        List<NotificationSubscribe> soldOutSubscribes = notificationSubscribeRepository
            .findAllBySubscribeType(DINING_SOLD_OUT);
        return soldOutSubscribes.stream()
            .map(soldOutSubscribe -> userRepository.getById(soldOutSubscribe.getUser().getId()))
            .map(User::getDeviceToken)
            .filter(deviceToken -> !deviceToken.isEmpty())
            .toList();
    }

    private void pushAll(Notification notification, List<String> deviceTokenList) {
        for (String device : deviceTokenList) {
            fcmClient.sendMessage(
                device,
                notification.getTitle(),
                notification.getMessage(),
                notification.getImageUrl(),
                notification.getMobileAppPath(),
                notification.getType()
            );
        }
    }

    private void push(Notification notification) {
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
        List<NotificationSubscribe> notificationSubscribes = notificationSubscribeRepository.findAllByUserId(userId);
        return NotificationStatusResponse.of(isPermit, notificationSubscribes);
    }

    @Transactional
    public void permitNotification(Integer userId, String deviceToken) {
        User user = userRepository.getById(userId);
        user.permitNotification(deviceToken);
    }

    @Transactional
    public void permitNotificationSubscribe(Integer userId, NotificationSubscribePermitRequest request) {
        if (notificationSubscribeRepository.findByUserIdAndSubscribeType(userId, request.type()).isPresent()) {
            return;
        }
        User user = userRepository.getById(userId);
        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .user(user)
            .subscribeType(request.type())
            .build();
        notificationSubscribeRepository.save(notificationSubscribe);
    }

    @Transactional
    public void rejectNotification(Integer userId) {
        User user = userRepository.getById(userId);
        user.rejectNotification();
    }

    @Transactional
    public void rejectNotificationByType(Integer userId, NotificationSubscribeType subscribeType) {
        notificationSubscribeRepository.deleteByUserIdAndSubscribeType(userId, subscribeType);
    }
}
